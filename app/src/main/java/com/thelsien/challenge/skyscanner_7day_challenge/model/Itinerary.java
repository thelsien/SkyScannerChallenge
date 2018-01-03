package com.thelsien.challenge.skyscanner_7day_challenge.model;

import org.json.JSONObject;

import java.util.List;

public class Itinerary {
    public String OutboundLegId;
    public String InboundLegId;
    public List<PricingOption> PricingOptions;
    public JSONObject BookingDetailsLink;
}
