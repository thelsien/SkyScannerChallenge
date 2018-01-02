package com.thelsien.challenge.skyscanner_7day_challenge;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thelsien.challenge.skyscanner_7day_challenge.model.Itinerary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adamszucs on 2018. 01. 02..
 */

public class LivePricingAdapter extends RecyclerView.Adapter<LivePricingAdapter.ViewHolder> {

    private static final String TAG = LivePricingAdapter.class.getSimpleName();

    private List<Itinerary> items = new ArrayList<>();

    public void addItems(List<Itinerary> newItems) {
        items.addAll(newItems);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_live_pricing, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Itinerary itinerary = items.get(position);

        holder.itineraryView.setText(itinerary.InboundLegId);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itineraryView;

        public ViewHolder(View itemView) {
            super(itemView);

            itineraryView = itemView.findViewById(R.id.tv_itinerary_id);
        }
    }
}
