package com.thelsien.challenge.skyscanner_7day_challenge;

import com.thelsien.challenge.skyscanner_7day_challenge.api.RetrofitHelper;

/**
 * Created by adamszucs on 2018. 01. 02..
 */

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
//                            Log.e(TAG, "error during getting session key", error);
                            mMainActivity.onObservableError(error);
                        }
                );
    }

    @Override
    public void pollLivePricing(String sessionKey) {
        RetrofitHelper.getPollingFlightDetailObservable(sessionKey)
                .subscribe(
                        flightDetail -> {
                            mMainActivity.onPollingFinished();
                        },
                        error -> {
//                            Log.e(TAG, "pollFlightDetails: error during getting flight detail", error);
                            mMainActivity.onObservableError(error);
                        }
                );
    }

    @Override
    public void getPaginatedLivePricing(String sessionKey, int pageIndex) {
        RetrofitHelper.getPaginatedFlightDetailObservable(sessionKey, pageIndex)
                .subscribe(
                        flightDetail -> {
                            mMainActivity.onPaginatedRequestFinished(flightDetail);
                        },
                        error -> {
//                            Log.e(TAG, "getPaginatedFlightDetails: error during paginated data query", error);
                            mMainActivity.onObservableError(error);
                        }
                );
    }
}
