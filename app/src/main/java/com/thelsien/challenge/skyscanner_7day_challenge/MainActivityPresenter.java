package com.thelsien.challenge.skyscanner_7day_challenge;

import com.thelsien.challenge.skyscanner_7day_challenge.api.RetrofitHelper;
import com.thelsien.challenge.skyscanner_7day_challenge.model.DataHelper;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Itinerary;
import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricingRowModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivityPresenter implements MainActivityContract.Presenter {

    public static final String TAG = MainActivityPresenter.class.getSimpleName();

    private MainActivityContract.View mMainActivity;

    public MainActivityPresenter(MainActivityContract.View mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void createPollingRequest() {
        RetrofitHelper.getCreatePollingObservable()
                .subscribe(
                        serverResponse -> {
                            final String[] urlParts = serverResponse.split("/");
                            mMainActivity.onSessionKeyReceived(urlParts[urlParts.length - 1]);
                        },
                        error -> {
                            mMainActivity.onObservableError(error);
                        }
                );
    }

    @Override
    public void pollLivePricing(String sessionKey) {
        RetrofitHelper.getPollingLivePricingObservable(sessionKey)
                .subscribe(
                        livePricing -> {
                            mMainActivity.onPollingFinished();
                        },
                        error -> {
                            mMainActivity.onObservableError(error);
                        }
                );
    }

    @Override
    public void getPaginatedLivePricing(String sessionKey, int pageIndex) {
        RetrofitHelper.getPaginatedLivePricingObservable(sessionKey, pageIndex)
                .subscribe(
                        livePricing -> {

                            List<LivePricingRowModel> livePricings = new ArrayList<>();

                            //TODO move code to separate class, DataManager?
                            for (Itinerary itinerary : livePricing.Itineraries) {
                                LivePricingRowModel rowModel = DataHelper.getRowModelData(livePricing, itinerary);
                                livePricings.add(rowModel);
                            }

                            mMainActivity.onPaginatedRequestFinished(livePricings);
                        },
                        error -> {
                            mMainActivity.onObservableError(error);
                        }
                );
    }
}
