package com.thelsien.challenge.skyscanner_7day_challenge;

import com.thelsien.challenge.skyscanner_7day_challenge.model.FlightDetail;
import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricingRowModel;

import java.util.List;

/**
 * Created by adamszucs on 2018. 01. 02..
 */

public class MainActivityContract {
    public interface View {
        void onSessionKeyReceived(String sessionKey);
        void onPollingFinished();
        void onPaginatedRequestFinished(List<LivePricingRowModel> livePricings);
        void onObservableError(Throwable error);
    }

    public interface Presenter {
        void createPollingRequest();
        void pollLivePricing(String sessionKey);
        void getPaginatedLivePricing(String sessionKey, int pageIndex);
    }
}
