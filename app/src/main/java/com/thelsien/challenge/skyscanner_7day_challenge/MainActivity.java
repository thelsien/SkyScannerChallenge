package com.thelsien.challenge.skyscanner_7day_challenge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.thelsien.challenge.skyscanner_7day_challenge.model.FlightDetail;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Itinerary;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityContract.Presenter mPresenter;
    private String mSessionKey;
    private int mPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new MainActivityPresenter(this);
        mPresenter.createPollingRequest();
    }

    @Override
    public void onSessionKeyReceived(String sessionKey) {
        mSessionKey = sessionKey;
        mPresenter.pollLivePricing(mSessionKey);
    }

    @Override
    public void onPollingFinished() {
        mPageIndex = 1;
        mPresenter.getPaginatedLivePricing(mSessionKey, mPageIndex);
    }

    @Override
    public void onPaginatedRequestFinished(FlightDetail flightDetail) {
        for (Itinerary itinerary : flightDetail.Itineraries) {
            Log.d(TAG, "pollFlightDetails paginated itinerary: " + itinerary.InboundLegId + " " + itinerary.OutboundLegId);
        }
    }

    @Override
    public void onObservableError(Throwable error) {
        Log.e(TAG, "onObservableError: some error happened", error);
    }
}
