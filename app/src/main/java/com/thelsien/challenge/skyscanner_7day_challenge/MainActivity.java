package com.thelsien.challenge.skyscanner_7day_challenge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.thelsien.challenge.skyscanner_7day_challenge.api.SkyScannerApiService;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Itinerary;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "MYAPIKEY";
    private String mSessionKey;
    private int mPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(SkyScannerApiService.BASE_URL)
                            .build();

                    SkyScannerApiService service = retrofit.create(SkyScannerApiService.class);

                    Map<String, String> queryOptions = new HashMap<>();
                    queryOptions.put("apikey", API_KEY); //TODO move to separate file
                    queryOptions.put("country", "UK");
                    queryOptions.put("currency", "GBP");
                    queryOptions.put("locale", "en-GB");
                    queryOptions.put("originPlace", "EDI-sky");
                    queryOptions.put("destinationPlace", "LHR-sky");
                    queryOptions.put("outbounddate", "2018-01-08");
                    queryOptions.put("inbounddate", "2018-01-09");
                    queryOptions.put("adults", "1");

                    Call<Void> pollingRequestCall = service.createPollingRequest(getIPAddress(true), queryOptions);

                    Response<Void> response = pollingRequestCall.execute();

                    e.onNext(response.headers().get("Location"));
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(serverResponse -> {
                            final String[] urlParts = serverResponse.split("/");
                            mSessionKey = urlParts[urlParts.length - 1];
                        },
                        error -> {
                            Log.e("SESSION_KEY_ERROR", "error during getting session key", error);
                        },
                        this::pollFlightDetails
                );
    }

    private void pollFlightDetails() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SkyScannerApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        final SkyScannerApiService service = retrofit.create(SkyScannerApiService.class);
        final Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("apikey", API_KEY); //TODO move to separate file
        queryOptions.put("pageIndex", String.valueOf(mPageIndex)); //initially 0.

        //query until we get UpdatesComplete status
        service.getFlightDetailObservable(mSessionKey, queryOptions)
                .repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                .takeUntil(flightDetail -> {
                    if (flightDetail.Status.equals("UpdatesComplete")) {
                        mPageIndex = 1;
                    }
                    return flightDetail.Status.equals("UpdatesComplete");
                })
                .filter(flightDetail -> flightDetail.Status.equals("UpdatesComplete"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(flightDetail -> {
                    mPageIndex = 1;
                    queryOptions.put("pageIndex", String.valueOf(mPageIndex));

                    //one more query with paging
                    service.getFlightDetailObservable(mSessionKey, queryOptions)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(flightDetail1 -> {
                                for (Itinerary itinerary : flightDetail.Itineraries) {
                                    Log.d("ITINERARY", "pollFlightDetails: " + itinerary.InboundLegId + " " + itinerary.OutboundLegId);
                                }
                            });
                });
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param ipv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }
}
