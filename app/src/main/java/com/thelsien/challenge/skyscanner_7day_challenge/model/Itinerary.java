package com.thelsien.challenge.skyscanner_7day_challenge.model;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by frodo on 2018-01-01.
 */

public class Itinerary {
    public String OutboundLegId;
    public String InboundLegId;
    public List<PricingOption> PricingOptions;
    public JSONObject BookingDetailsLink;
}
