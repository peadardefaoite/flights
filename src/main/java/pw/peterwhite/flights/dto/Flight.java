package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonAutoDetect
public class Flight {
    public int stops;
    public List<Leg> legs;

    @JsonCreator
    public Flight(@JsonProperty("stops") int stops,
                  @JsonProperty("legs") List<Leg> legs) {
        this.stops = stops;
        this.legs = legs;
    }

    public static class Leg {
        public String departureAirport;
        public String arrivalAirport;
        public LocalDateTime departureTime;
        public LocalDateTime arrivalTime;

        @JsonCreator
        public Leg(@JsonProperty("departureAirport") String departureAirport,
                   @JsonProperty("arrivalAirport") String arrivalAirport,
                   @JsonProperty("departureTime") LocalDateTime departureTime,
                   @JsonProperty("arrivalTime") LocalDateTime arrivalTime) {
            this.departureAirport = departureAirport;
            this.arrivalAirport = arrivalAirport;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }
    }
}
