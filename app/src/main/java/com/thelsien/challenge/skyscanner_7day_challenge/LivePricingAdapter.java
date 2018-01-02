package com.thelsien.challenge.skyscanner_7day_challenge;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricingRowModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by adamszucs on 2018. 01. 02..
 */

public class LivePricingAdapter extends RecyclerView.Adapter<LivePricingAdapter.ViewHolder> {

    private static final String TAG = LivePricingAdapter.class.getSimpleName();

    private List<LivePricingRowModel> items = new ArrayList<>();

    public void addItems(List<LivePricingRowModel> newItems) {
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
//        Itinerary itinerary = items.get(position);

        LivePricingRowModel rowModel = items.get(position);

        holder.inboundLogo.setText(rowModel.inboundCarrierLogoUrl);
        holder.outboundLogo.setText(rowModel.outboundCarrierLogoUrl);

        Calendar c = Calendar.getInstance();
        c.setTime(rowModel.inboundDepartureDate);
        holder.inboundDeparture.setText(String.format(Locale.getDefault(), "%d-%d-%d %02d:%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        c.setTime(rowModel.inboundArrivalDate);
        holder.inboundArrival.setText(String.format(Locale.getDefault(), "%d-%d-%d %02d:%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        c.setTime(rowModel.outboundDepartureDate);
        holder.outboundDeparture.setText(String.format(Locale.getDefault(), "%d-%d-%d %02d:%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        c.setTime(rowModel.outboundArrivalDate);
        holder.outboundArrival.setText(String.format(Locale.getDefault(), "%d-%d-%d %02d:%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));

        holder.inboundStationsCarrier.setText(String.format(Locale.getDefault(), "%s-%s, %s", rowModel.inboundOriginStationName, rowModel.inboundDestinationStationName, rowModel.inboundCarrierName));
        holder.outboundStationsCarrier.setText(String.format(Locale.getDefault(), "%s-%s, %s", rowModel.outboundOriginStationName, rowModel.outboundDestinationStationName, rowModel.outboundCarrierName));

        holder.inboundDirectionality.setText(rowModel.inboundDirectionality);
        holder.outboundDirectionality.setText(rowModel.outboundDirectionality);

        Pair<Integer, Integer> hoursAndMinutes = getHourAndMinutes(rowModel.inboundDuration);
        holder.inboundDuration.setText(String.format(Locale.getDefault(), "%dh %dm", hoursAndMinutes.first, hoursAndMinutes.second));
        hoursAndMinutes = getHourAndMinutes(rowModel.outboundDuration);
        holder.outboundDuration.setText(String.format(Locale.getDefault(), "%dh %dm", hoursAndMinutes.first, hoursAndMinutes.second));

        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator(rowModel.priceCurrencyInfo.DecimalSeparator.charAt(0));
        symbol.setGroupingSeparator(rowModel.priceCurrencyInfo.ThousandsSeparator.charAt(0));
        symbol.setCurrencySymbol(rowModel.priceCurrencyInfo.Symbol);
        decimalFormat.setDecimalFormatSymbols(symbol);
        decimalFormat.setMaximumFractionDigits(rowModel.priceCurrencyInfo.DecimalDigits);
        decimalFormat.setMinimumFractionDigits(rowModel.priceCurrencyInfo.DecimalDigits);

        String priceString = decimalFormat.format(rowModel.price);
        if (rowModel.priceCurrencyInfo.SymbolOnLeft) {
            priceString = rowModel.priceCurrencyInfo.Symbol +
                    (rowModel.priceCurrencyInfo.SpaceBetweenAmountAndSymbol ? " " : "") +
                    priceString;
        } else {
            priceString = priceString +
                    (rowModel.priceCurrencyInfo.SpaceBetweenAmountAndSymbol ? " " : "") +
                    rowModel.priceCurrencyInfo.Symbol;
        }
        holder.price.setText(priceString);
        holder.agent.setText(rowModel.agentName);
    }

    private Pair<Integer, Integer> getHourAndMinutes(int duration) {
        int hours, minutes;

        hours = duration / 60;
        minutes = duration - (hours * 60);

        return new Pair<>(hours, minutes);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView outboundLogo;
        private TextView outboundDeparture;
        private TextView outboundArrival;
        private TextView inboundDeparture;
        private TextView inboundArrival;
        private TextView inboundLogo;
        private TextView inboundStationsCarrier;
        private TextView outboundStationsCarrier;
        private TextView inboundDirectionality;
        private TextView outboundDirectionality;
        private TextView inboundDuration;
        private TextView outboundDuration;
        private TextView price;
        private TextView agent;

        public ViewHolder(View itemView) {
            super(itemView);

            inboundLogo = itemView.findViewById(R.id.tv_inboundlogo);
            outboundLogo = itemView.findViewById(R.id.tv_outboundlogo);
            outboundDeparture = itemView.findViewById(R.id.tv_outbound_departure);
            outboundArrival = itemView.findViewById(R.id.tv_outbound_arrival);
            inboundDeparture = itemView.findViewById(R.id.tv_inbound_departure);
            inboundArrival = itemView.findViewById(R.id.tv_inbound_arrival);
            inboundStationsCarrier = itemView.findViewById(R.id.tv_inbound_stations_carrier);
            outboundStationsCarrier = itemView.findViewById(R.id.tv_outbound_stations_carrier);
            inboundDirectionality = itemView.findViewById(R.id.tv_inbound_directionality);
            outboundDirectionality = itemView.findViewById(R.id.tv_outbound_directionality);
            inboundDuration = itemView.findViewById(R.id.tv_inbound_duration);
            outboundDuration = itemView.findViewById(R.id.tv_outbound_duration);
            price = itemView.findViewById(R.id.tv_price);
            agent = itemView.findViewById(R.id.tv_agent);
        }
    }
}
