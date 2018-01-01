package com.thelsien.challenge.skyscanner_7day_challenge.model;

import java.util.List;

/**
 * Created by frodo on 2018-01-01.
 */

public class FlightDetail {
    public String SessionKey;
    public String Status;
    public List<Itinerary> Itineraries;
    public List<Leg> Legs;
    public List<Segment> Segments;
    public List<Carrier> Carriers;
    public List<Agent> Agents;
    public List<Place> Places;
    public List<Currency> Currencies;
}
