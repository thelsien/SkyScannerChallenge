package com.thelsien.challenge.skyscanner_7day_challenge;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricingRowModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
        LivePricingRowModel rowModel = items.get(position);

        loadLogos(holder, rowModel);
        setDeparturesAndArrivals(holder, rowModel);
        setStationsAndCarriers(holder, rowModel);
        setDirectionalities(holder, rowModel);
        setDurations(holder, rowModel);
        setPriceAndAgent(holder, rowModel);
    }

    private void loadLogos(ViewHolder holder, LivePricingRowModel rowModel) {
        Glide.with(holder.inboundLogo)
                .load(rowModel.inboundCarrierLogoUrl)
                .into(holder.inboundLogo);

        Glide.with(holder.outboundLogo)
                .load(rowModel.outboundCarrierLogoUrl)
                .into(holder.outboundLogo);
    }

    private void setDeparturesAndArrivals(ViewHolder holder, LivePricingRowModel rowModel) {
        Calendar calDeparture = Calendar.getInstance();
        Calendar calArrival = Calendar.getInstance();

        calDeparture.setTime(rowModel.inboundDepartureDate);
        calArrival.setTime(rowModel.inboundArrivalDate);
        holder.inboundDepartureArrival.setText(String.format(
                Locale.getDefault(),
                holder.inboundDepartureArrival.getContext().getString(R.string.departure_arrival_template),
                calDeparture.get(Calendar.HOUR_OF_DAY),
                calDeparture.get(Calendar.MINUTE),
                calArrival.get(Calendar.HOUR_OF_DAY),
                calArrival.get(Calendar.MINUTE)
        ));

        calDeparture.setTime(rowModel.outboundDepartureDate);
        calArrival.setTime(rowModel.outboundArrivalDate);
        holder.outboundDepartureArrival.setText(String.format(
                Locale.getDefault(),
                holder.outboundDepartureArrival.getContext().getString(R.string.departure_arrival_template),
                calDeparture.get(Calendar.HOUR_OF_DAY),
                calDeparture.get(Calendar.MINUTE),
                calArrival.get(Calendar.HOUR_OF_DAY),
                calArrival.get(Calendar.MINUTE)
        ));
    }

    private void setStationsAndCarriers(ViewHolder holder, LivePricingRowModel rowModel) {
        holder.inboundStationsCarrier.setText(String.format(
                Locale.getDefault(),
                holder.inboundStationsCarrier.getContext().getString(R.string.stations_carrier_template),
                rowModel.inboundOriginStationName,
                rowModel.inboundDestinationStationName,
                rowModel.inboundCarrierName
        ));
        holder.outboundStationsCarrier.setText(String.format(
                Locale.getDefault(),
                holder.outboundStationsCarrier.getContext().getString(R.string.stations_carrier_template),
                rowModel.outboundOriginStationName,
                rowModel.outboundDestinationStationName,
                rowModel.outboundCarrierName
        ));
    }

    private void setDirectionalities(ViewHolder holder, LivePricingRowModel rowModel) {
        holder.inboundDirectionality.setText(rowModel.inboundDirectionality);
        holder.outboundDirectionality.setText(rowModel.outboundDirectionality);
    }

    private void setDurations(ViewHolder holder, LivePricingRowModel rowModel) {
        Pair<Integer, Integer> hoursAndMinutes = getHourAndMinutes(rowModel.inboundDuration);
        holder.inboundDuration.setText(String.format(
                Locale.getDefault(),
                holder.inboundDuration.getContext().getString(R.string.duration_template),
                hoursAndMinutes.first,
                hoursAndMinutes.second
        ));

        hoursAndMinutes = getHourAndMinutes(rowModel.outboundDuration);
        holder.outboundDuration.setText(String.format(
                Locale.getDefault(),
                holder.outboundDuration.getContext().getString(R.string.duration_template),
                hoursAndMinutes.first,
                hoursAndMinutes.second
        ));
    }

    private void setPriceAndAgent(ViewHolder holder, LivePricingRowModel rowModel) {
        String priceString = getPriceString(rowModel);
        holder.price.setText(priceString);
        holder.agent.setText(String.format(
                holder.agent.getContext().getString(R.string.price_agent),
                rowModel.agentName
        ));
    }

    @NonNull
    private String getPriceString(LivePricingRowModel rowModel) {
        DecimalFormat decimalFormat = getDecimalFormatForCurrency(rowModel);
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
        return priceString;
    }

    @NonNull
    private DecimalFormat getDecimalFormatForCurrency(LivePricingRowModel rowModel) {
        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator(rowModel.priceCurrencyInfo.DecimalSeparator.charAt(0));
        symbol.setGroupingSeparator(rowModel.priceCurrencyInfo.ThousandsSeparator.charAt(0));
        symbol.setCurrencySymbol(rowModel.priceCurrencyInfo.Symbol);
        decimalFormat.setDecimalFormatSymbols(symbol);
        decimalFormat.setMaximumFractionDigits(rowModel.priceCurrencyInfo.DecimalDigits);
        decimalFormat.setMinimumFractionDigits(rowModel.priceCurrencyInfo.DecimalDigits);
        return decimalFormat;
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

        private ImageView outboundLogo;
        private ImageView inboundLogo;
        private TextView outboundDepartureArrival;
        private TextView inboundDepartureArrival;
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
            outboundDepartureArrival = itemView.findViewById(R.id.tv_outbound_departure_arrival);
            inboundDepartureArrival = itemView.findViewById(R.id.tv_inbound_departure_arrival);
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
