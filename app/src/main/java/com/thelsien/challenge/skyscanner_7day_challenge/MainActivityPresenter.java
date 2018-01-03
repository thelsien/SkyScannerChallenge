package com.thelsien.challenge.skyscanner_7day_challenge;

import android.util.Log;

import com.thelsien.challenge.skyscanner_7day_challenge.api.RetrofitHelper;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Agent;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Carrier;
import com.thelsien.challenge.skyscanner_7day_challenge.model.FlightDetail;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Itinerary;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Leg;
import com.thelsien.challenge.skyscanner_7day_challenge.model.LivePricingRowModel;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Place;
import com.thelsien.challenge.skyscanner_7day_challenge.model.PricingOption;
import com.thelsien.challenge.skyscanner_7day_challenge.model.Segment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                            List<LivePricingRowModel> livePricings = new ArrayList<>();

                            //TODO move code to separate class, DataManager?
                            for (Itinerary itinerary : flightDetail.Itineraries) {
                                LivePricingRowModel rowModel = new LivePricingRowModel();

                                rowModel = fillRowFromLegDetails(flightDetail, rowModel, itinerary.InboundLegId, true);
                                rowModel = fillRowFromLegDetails(flightDetail, rowModel, itinerary.OutboundLegId, false);

                                PricingOption pricingOption = itinerary.PricingOptions.get(0); //only getting the first one for this demo app
                                rowModel.price = pricingOption.Price;

                                for (Agent agent : flightDetail.Agents) {
                                    if (agent.Id == pricingOption.Agents.get(0)) {
                                        rowModel.agentName = agent.Name;
                                        break;
                                    }
                                }

                                rowModel.priceCurrencyInfo = flightDetail.Currencies.get(0);

                                livePricings.add(rowModel);
                            }

                            mMainActivity.onPaginatedRequestFinished(livePricings);
                        },
                        error -> {
//                            Log.e(TAG, "getPaginatedFlightDetails: error during paginated data query", error);
                            mMainActivity.onObservableError(error);
                        }
                );
    }

    //TODO separate class, DataManager?
    private LivePricingRowModel fillRowFromLegDetails(FlightDetail flightDetail, LivePricingRowModel rowModel, String legId, boolean isInboundLegId) {
        Leg leg = null;
        for (Leg flightLeg : flightDetail.Legs) {
            if (flightLeg.Id.equals(legId)) {
                leg = flightLeg;
                break;
            }
        }

        //carrier logo, name
        int carrierId = leg.Carriers.get(0);
        for (Carrier carrier : flightDetail.Carriers) {
            if (carrier.Id == carrierId) {
                String imgUrl = LivePricingRowModel.BASE_IMAGE_URL + carrier.Code + ".png";
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

        //departure/arrival date
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

        //station names
        if (isInboundLegId) {
            rowModel.inboundOriginStationName = getStationName(flightDetail.Places, leg.OriginStation);
            rowModel.inboundDestinationStationName = getStationName(flightDetail.Places, leg.DestinationStation);
        } else {
            rowModel.outboundOriginStationName = getStationName(flightDetail.Places, leg.OriginStation);
            rowModel.outboundDestinationStationName = getStationName(flightDetail.Places, leg.DestinationStation);
        }

        //directionality, duration
        //only taking the first, because it does not contain more than one for this distance.
        int segmentId = leg.SegmentIds.get(0);
        for (Segment segment : flightDetail.Segments) {
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

        return rowModel;
    }

    //TODO separate class, DataManager?
    private String getStationName(List<Place> places, int stationId) {
        for (Place place : places) {
            if (place.Id == stationId) {
                return place.Code;
            }
        }
        return "";
    }
}
