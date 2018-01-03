package com.thelsien.challenge.skyscanner_7day_challenge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricingRowModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int VISIBLE_THRESHOLD = 1;

    private MainActivityContract.Presenter mPresenter;
    private String mSessionKey;
    private int mPageIndex = 0;
    private RecyclerView rvList;
    private boolean isLoading = false;
    private int lastVisibleItem, totalItemCount;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvList = findViewById(R.id.rv_list);
        toolbar = findViewById(R.id.tb_toolbar);

        setupToolbar();
        setupListView();
        setupPagination();

        mPresenter = new MainActivityPresenter(this);
        isLoading = true;
        mPresenter.createPollingRequest();
    }

    private void setupToolbar() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        Calendar calendar = Utils.getNextMonday();
        Calendar calendarNextDay = Calendar.getInstance();
        calendarNextDay.setTime(calendar.getTime());
        calendarNextDay.add(Calendar.DATE, 1);

        toolbar.setTitle(R.string.route_name);
        toolbar.setSubtitle(String.format(
                getString(R.string.route_subtitle),
                dateFormat.format(calendar.getTime()),
                dateFormat.format(calendarNextDay.getTime()),
                1
        ));
        setSupportActionBar(toolbar);
    }

    private void setupListView() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(layoutManager);
    }

    private void setupPagination() {
        rvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager
                        .findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                    mPageIndex++;
                    mPresenter.getPaginatedLivePricing(mSessionKey, mPageIndex);
                    isLoading = true;
                }
            }
        });
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
    public void onPaginatedRequestFinished(List<LivePricingRowModel> livePricings) {
        isLoading = false;

        addItemsToList(livePricings);
    }

    private void addItemsToList(List<LivePricingRowModel> livePricings) {
        if (rvList.getAdapter() == null) {
            rvList.setAdapter(new LivePricingAdapter());
        }

        ((LivePricingAdapter) rvList.getAdapter()).addItems(livePricings);
        rvList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onObservableError(Throwable error) {
        Log.e(TAG, "onObservableError: some error happened", error);
        isLoading = false;
    }
}
