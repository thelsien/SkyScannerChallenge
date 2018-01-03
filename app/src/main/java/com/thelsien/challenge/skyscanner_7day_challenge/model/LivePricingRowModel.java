package com.thelsien.challenge.skyscanner_7day_challenge.model;

import org.json.JSONObject;

import java.util.Date;

public class LivePricingRowModel {
    public static final String BASE_IMAGE_URL = "https://logos.skyscnr.com/images/airlines/favicon/";

    public String inboundCarrierLogoUrl;
    public String inboundCarrierName;
    public String outboundCarrierLogoUrl;
    public String outboundCarrierName;

    public Date inboundDepartureDate;
    public Date inboundArrivalDate;

    public Date outboundDepartureDate;
    public Date outboundArrivalDate;

    public String inboundOriginStationName;
    public String inboundDestinationStationName;

    public String outboundOriginStationName;
    public String outboundDestinationStationName;

    public String inboundDirectionality;
    public String outboundDirectionality;

    public int inboundDuration;
    public int outboundDuration;

    public float price;
    public Currency priceCurrencyInfo;
    public String agentName;
}
