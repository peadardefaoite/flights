package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect
public class Schedule {
    public int month;
    public List<FlightDays> days;

    public static class FlightDays {
        public int day;
        public List<FlightDetails> flights;
    }

    public static class FlightDetails {
        public String carrierCode;
        public String number;
        public String departureTime;
        public String arrivalTime;
    }
}
