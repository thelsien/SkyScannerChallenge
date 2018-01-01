package com.thelsien.challenge.skyscanner_7day_challenge.model;

import java.util.List;

/**
 * Created by frodo on 2018-01-01.
 */

public class Leg {
    public String Id;
    public List<Integer> SegmentIds;
    public int OriginStation;
    public int DestinationStation;
    public String Departure;
    public String Arrival;
    public int Duration;
    public String JourneyMode;
    public List<Integer> Stops;
    public List<Integer> Carriers;
    public List<Integer> OperatingCarriers;
    public String Directionality;
    public List<FlightNumber> FlightNumbers;
}
