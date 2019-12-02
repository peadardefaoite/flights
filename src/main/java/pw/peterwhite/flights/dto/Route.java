package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonCreator
    public Route(@JsonProperty("airportFrom") String airportFrom,
                 @JsonProperty("airportTo") String airportTo,
                 @JsonProperty("connectingAirport") String connectingAirport,
                 @JsonProperty("newRoute") boolean newRoute,
                 @JsonProperty("seasonalRoute") boolean seasonalRoute,
                 @JsonProperty("operator") String operator,
                 @JsonProperty("group") String group,
                 @JsonProperty("similarArrivalAirportCodes") List<String> similarArrivalAirportCodes,
                 @JsonProperty("tags") List<String> tags,
                 @JsonProperty("carrierCode") String carrierCode) {
        this.airportFrom = airportFrom;
        this.airportTo = airportTo;
        this.connectingAirport = connectingAirport;
        this.newRoute = newRoute;
        this.seasonalRoute = seasonalRoute;
        this.operator = operator;
        this.group = group;
        this.similarArrivalAirportCodes = similarArrivalAirportCodes;
        this.tags = tags;
        this.carrierCode = carrierCode;
    }
}