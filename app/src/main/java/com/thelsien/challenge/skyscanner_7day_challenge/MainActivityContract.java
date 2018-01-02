package com.thelsien.challenge.skyscanner_7day_challenge;

import com.thelsien.challenge.skyscanner_7day_challenge.model.FlightDetail;

/**
 * Created by adamszucs on 2018. 01. 02..
 */

public class MainActivityContract {
    public interface View {
        void onSessionKeyReceived(String sessionKey);
        void onPollingFinished();
        void onPaginatedRequestFinished(FlightDetail flightDetail);
        void onObservableError(Throwable error);
    }

    public interface Presenter {
        void createPollingRequest();
        void pollLivePricing(String sessionKey);
        void getPaginatedLivePricing(String sessionKey, int pageIndex);
    }
}
