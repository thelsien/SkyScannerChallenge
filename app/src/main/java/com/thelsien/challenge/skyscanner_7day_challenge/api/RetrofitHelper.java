package com.thelsien.challenge.skyscanner_7day_challenge.api;

import android.util.Log;

import com.thelsien.challenge.skyscanner_7day_challenge.Utils;
import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricing;

import java.util.Calendar;
import java.util.HashMap;
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

public class RetrofitHelper {

    private static final String TAG = RetrofitHelper.class.getSimpleName();
    private static final String API_KEY = "MYAPIKEY";

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

                    Calendar calendar = Utils.getNextMonday();

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

                    Call<Void> pollingRequestCall = service.createPollingRequest(Utils.getIPAddress(true), queryOptions);

                    Response<Void> response = pollingRequestCall.execute();
                    e.onNext(response.headers().get("Location"));
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<LivePricing> getPollingLivePricingObservable(String sessionKey) {
        final SkyScannerApiService service = RetrofitHelper
                .getGsonWithObservableRetrofit(SkyScannerApiService.BASE_URL)
                .create(SkyScannerApiService.class);

        final Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("apikey", API_KEY);
        queryOptions.put("pageIndex", String.valueOf(0)); //initially 0.

        //query until we get UpdatesComplete status
        return service.getFlightDetailObservable(sessionKey, queryOptions)
                .repeatWhen(objectObservable -> objectObservable.delay(1, TimeUnit.SECONDS))
                .takeUntil(livePricing -> {
                    return livePricing.Status.equals("UpdatesComplete");
                })
                .retryWhen(errorsObservable -> errorsObservable.flatMap(error -> {
                    if (error instanceof HttpException) {
                        Log.e(TAG, "getPollingLivePricingObservable: httpexception", error);
                        LivePricing fd = new LivePricing();
                        fd.Status = "UpdatesPending";
                        return Observable.just(fd).delay(1, TimeUnit.SECONDS);
                    }

                    return Observable.error(error);
                }))
                .filter(livePricing -> livePricing.Status.equals("UpdatesComplete"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<LivePricing> getPaginatedLivePricingObservable(String sessionKey, int pageIndex) {
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
}
