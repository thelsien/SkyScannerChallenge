package com.thelsien.challenge.skyscanner_7day_challenge.api;

import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricing;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface SkyScannerApiService {
    String BASE_URL = "http://partners.api.skyscanner.net/apiservices/";

    @Headers({
            "Accept: application/json"
    })
    @FormUrlEncoded
    @POST("pricing/v1.0")
    Call<Void> createPollingRequest(@Header("X-Forwarded-For") String ip, @FieldMap Map<String, String> postParams);

    @Headers("Accept: application/json")
    @GET("pricing/v1.0/{SessionKey}")
    Observable<LivePricing> getFlightDetailObservable(@Path("SessionKey") String sessionKey, @QueryMap Map<String, String> getParams);
}
