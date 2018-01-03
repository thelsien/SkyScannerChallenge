package com.thelsien.challenge.skyscanner_7day_challenge.api;

import android.util.Log;

import com.thelsien.challenge.skyscanner_7day_challenge.model.FlightDetail;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by adamszucs on 2018. 01. 02..
 */

public class RetrofitHelper {

    private static final String TAG = RetrofitHelper.class.getSimpleName();
    private static final String API_KEY = "ss630745725358065467897349852985";

    public static Retrofit getBaseRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
    }

    public static Retrofit getGsonWithObservableRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static Observable<String> getCreatePollingObservable() {
        return Observable
                .create((ObservableOnSubscribe<String>) e -> {

                    SkyScannerApiService service = RetrofitHelper
                            .getBaseRetrofit(SkyScannerApiService.BASE_URL)
                            .create(SkyScannerApiService.class);

                    Calendar calendar = Calendar.getInstance();
                    //task specified we need to get the NEXT monday's live pricing,
                    //so even if today is monday, I skip to the next monday.
                    if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    Map<String, String> queryOptions = new HashMap<>();
                    queryOptions.put("apikey", API_KEY);
                    queryOptions.put("country", "UK");
                    queryOptions.put("currency", "GBP");
                    queryOptions.put("locale", "en-GB");
                    queryOptions.put("originPlace", "EDI-sky");
                    queryOptions.put("destinationPlace", "LHR-sky");
                    queryOptions.put("outbounddate", android.text.format.DateFormat.format("yyyy-MM-dd", calendar).toString());
                    calendar.add(Calendar.DATE, 1);
                    queryOptions.put("inbounddate", android.text.format.DateFormat.format("yyyy-MM-dd", calendar).toString());
                    queryOptions.put("adults", "1");

                    Call<Void> pollingRequestCall = service.createPollingRequest(getIPAddress(true), queryOptions);

                    Response<Void> response = pollingRequestCall.execute();

                    e.onNext(response.headers().get("Location"));
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FlightDetail> getPollingFlightDetailObservable(String sessionKey) {
        final SkyScannerApiService service = RetrofitHelper
                .getGsonWithObservableRetrofit(SkyScannerApiService.BASE_URL)
                .create(SkyScannerApiService.class);

        final Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("apikey", API_KEY);
        queryOptions.put("pageIndex", String.valueOf(0)); //initially 0.

        //query until we get UpdatesComplete status
        return service.getFlightDetailObservable(sessionKey, queryOptions)
                .repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                .takeUntil(flightDetail -> {
                    return flightDetail.Status.equals("UpdatesComplete");
                })
                .retryWhen(errorsObservable -> errorsObservable.flatMap(error -> {
                    if (error instanceof HttpException) {
                        Log.e(TAG, "getPollingFlightDetailObservable: httpexception", error);
                        return Observable.just(null);
                    }

                    return Observable.error(error);
                }))
                .filter(flightDetail -> flightDetail.Status.equals("UpdatesComplete"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<FlightDetail> getPaginatedFlightDetailObservable(String sessionKey, int pageIndex) {
        final SkyScannerApiService service = RetrofitHelper
                .getGsonWithObservableRetrofit(SkyScannerApiService.BASE_URL)
                .create(SkyScannerApiService.class);

        final Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("apikey", API_KEY);
        queryOptions.put("pageIndex", String.valueOf(pageIndex));

        //one more query with paging
        return service.getFlightDetailObservable(sessionKey, queryOptions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    private static String getIPAddress(boolean useIPv4) {
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
