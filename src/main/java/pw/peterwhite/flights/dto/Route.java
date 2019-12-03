package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the response returned from Routes API
 * Only populated with values that are necessary for our microservice
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Route {
    private String airportFrom;
    private String airportTo;
    private String connectingAirport;
    private String operator;

    @JsonCreator
    public Route(@JsonProperty("airportFrom") String airportFrom,
                 @JsonProperty("airportTo") String airportTo,
                 @JsonProperty("connectingAirport") String connectingAirport,
                 @JsonProperty("operator") String operator) {
        this.airportFrom = airportFrom;
        this.airportTo = airportTo;
        this.connectingAirport = connectingAirport;
        this.operator = operator;
    }

    public String getAirportFrom() {
        return airportFrom;
    }

    public void setAirportFrom(String airportFrom) {
        this.airportFrom = airportFrom;
    }

    public String getAirportTo() {
        return airportTo;
    }

    public void setAirportTo(String airportTo) {
        this.airportTo = airportTo;
    }

    public String getConnectingAirport() {
        return connectingAirport;
    }

    public void setConnectingAirport(String connectingAirport) {
        this.connectingAirport = connectingAirport;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Route{" +
                "airportFrom='" + airportFrom + '\'' +
                ", airportTo='" + airportTo + '\'' +
                ", connectingAirport='" + connectingAirport + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}