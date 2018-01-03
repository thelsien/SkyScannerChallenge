package com.thelsien.challenge.skyscanner_7day_challenge.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DataHelper {
    private static final String TAG = DataHelper.class.getSimpleName();

    @NonNull
    public static LivePricingAdapterRow getRowModelData(LivePricing livePricing, Itinerary itinerary) {
        LivePricingAdapterRow rowModel = new LivePricingAdapterRow();

        fillRowFromLegDetails(livePricing, rowModel, itinerary.InboundLegId, true);
        fillRowFromLegDetails(livePricing, rowModel, itinerary.OutboundLegId, false);

        getPricingAndAgentForRowModel(livePricing, itinerary, rowModel);

        rowModel.priceCurrencyInfo = livePricing.Currencies.get(0);
        return rowModel;
    }

    private static void getPricingAndAgentForRowModel(LivePricing livePricing, Itinerary itinerary, LivePricingAdapterRow rowModel) {
        PricingOption pricingOption = itinerary.PricingOptions.get(0); //only getting the first one for this demo app
        rowModel.price = pricingOption.Price;

        for (Agent agent : livePricing.Agents) {
            if (agent.Id == pricingOption.Agents.get(0)) {
                rowModel.agentName = agent.Name;
                break;
            }
        }
    }

    private static void fillRowFromLegDetails(LivePricing livePricing, LivePricingAdapterRow rowModel, String legId, boolean isInboundLegId) {
        Leg leg = findLeg(livePricing, legId);

        if (leg == null) {
            return;
        }

        getCarrierInfo(livePricing, rowModel, isInboundLegId, leg);
        getDepartureArrivalDate(rowModel, isInboundLegId, leg);
        getStationNames(livePricing, rowModel, isInboundLegId, leg);
        getDirectionalityAndDuration(livePricing, rowModel, isInboundLegId, leg);
    }

    private static void getDirectionalityAndDuration(LivePricing livePricing, LivePricingAdapterRow rowModel, boolean isInboundLegId, Leg leg) {
        //only taking the first, because it does not contain more than one for this distance.
        int segmentId = leg.SegmentIds.get(0);
        for (Segment segment : livePricing.Segments) {
            if (segment.Id == segmentId) {
                if (isInboundLegId) {
                    rowModel.inboundDirectionality = segment.Directionality;
                    rowModel.inboundDuration = segment.Duration;
                } else {
                    rowModel.outboundDirectionality = segment.Directionality;
                    rowModel.outboundDuration = segment.Duration;
                }
                break;
            }
        }
    }

    private static void getStationNames(LivePricing livePricing, LivePricingAdapterRow rowModel, boolean isInboundLegId, Leg leg) {
        if (isInboundLegId) {
            rowModel.inboundOriginStationName = getStationName(livePricing.Places, leg.OriginStation);
            rowModel.inboundDestinationStationName = getStationName(livePricing.Places, leg.DestinationStation);
        } else {
            rowModel.outboundOriginStationName = getStationName(livePricing.Places, leg.OriginStation);
            rowModel.outboundDestinationStationName = getStationName(livePricing.Places, leg.DestinationStation);
        }
    }

    private static void getDepartureArrivalDate(LivePricingAdapterRow rowModel, boolean isInboundLegId, Leg leg) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault());
        try {
            if (isInboundLegId) {
                rowModel.inboundDepartureDate = dateFormat.parse(leg.Departure);
                rowModel.inboundArrivalDate = dateFormat.parse(leg.Arrival);
            } else {
                rowModel.outboundDepartureDate = dateFormat.parse(leg.Departure);
                rowModel.outboundArrivalDate = dateFormat.parse(leg.Arrival);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "fillRowFromLegDetails: date parse error - " + leg.Departure + " - " + leg.Arrival, e);
        }
    }

    private static void getCarrierInfo(LivePricing livePricing, LivePricingAdapterRow rowModel, boolean isInboundLegId, Leg leg) {
        int carrierId = leg.Carriers.get(0);
        for (Carrier carrier : livePricing.Carriers) {
            if (carrier.Id == carrierId) {
                String imgUrl = LivePricingAdapterRow.BASE_IMAGE_URL + carrier.Code + ".png";
                if (isInboundLegId) {
                    rowModel.inboundCarrierLogoUrl = imgUrl;
                    rowModel.inboundCarrierName = carrier.Name;
                } else {
                    rowModel.outboundCarrierLogoUrl = imgUrl;
                    rowModel.outboundCarrierName = carrier.Name;
                }
                break;
            }
        }
    }

    @Nullable
    private static Leg findLeg(LivePricing livePricing, String legId) {
        Leg leg = null;
        for (Leg flightLeg : livePricing.Legs) {
            if (flightLeg.Id.equals(legId)) {
                leg = flightLeg;
                break;
            }
        }
        return leg;
    }

    private static String getStationName(List<Place> places, int stationId) {
        for (Place place : places) {
            if (place.Id == stationId) {
                return place.Code;
            }
        }
        return "";
    }
}
