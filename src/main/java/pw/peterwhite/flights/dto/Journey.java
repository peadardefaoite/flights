package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for the response returned from the Interconnections API
 * /api/{version}/interconnections?...
 */

@JsonAutoDetect
public class Journey {
    private int stops;
    private List<Leg> legs;

    @JsonCreator
    public Journey(@JsonProperty("stops") int stops,
                   @JsonProperty("legs") List<Leg> legs) {
        this.stops = stops;
        this.legs = legs;
    }

    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "stops=" + stops +
                ", legs=" + legs +
                '}';
    }

    public static class Leg {
        private String departureAirport;
        private String arrivalAirport;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;

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

        public String getDepartureAirport() {
            return departureAirport;
        }

        public void setDepartureAirport(String departureAirport) {
            this.departureAirport = departureAirport;
        }

        public String getArrivalAirport() {
            return arrivalAirport;
        }

        public void setArrivalAirport(String arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
        }

        public LocalDateTime getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
        }

        public LocalDateTime getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(LocalDateTime arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        @Override
        public String toString() {
            return "Leg{" +
                    "departureAirport='" + departureAirport + '\'' +
                    ", arrivalAirport='" + arrivalAirport + '\'' +
                    ", departureTime=" + departureTime +
                    ", arrivalTime=" + arrivalTime +
                    '}';
        }
    }
}
