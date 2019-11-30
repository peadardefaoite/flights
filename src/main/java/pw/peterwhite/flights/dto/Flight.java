package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDateTime;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Flight {
    public int stops;
    public List<Leg> legs;

    public static class Leg {
        public String departureAirport;
        public String arrivalAirport;
        public LocalDateTime departureTime;
        public LocalDateTime arrivalTime;
    }
}
