package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect
public class Route {
    public String airportFrom;
    public String airportTo;
    public String connectingAirport;
    public boolean newRoute;
    public boolean seasonalRoute;
    public String operator;
    public String group;
    public List<String> similarArrivalAirportCodes;
    public List<String> tags;
    public String carrierCode;
}
