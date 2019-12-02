package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonAutoDetect
public class Schedule {
    public int month;
    public List<FlightDays> days;

    @JsonCreator
    public Schedule(@JsonProperty("month") int month,
                    @JsonProperty("days") List<FlightDays> days) {
        this.month = month;
        this.days = days;
    }

    public static class FlightDays {
        public int day;
        public List<FlightDetails> flights;

        public FlightDays(@JsonProperty("day") int day,
                          @JsonProperty("flights") List<FlightDetails> flights) {
            this.day = day;
            this.flights = flights;
        }
    }

    public static class FlightDetails {
        public String carrierCode;
        public String number;
        public String departureTime;
        public String arrivalTime;

        public FlightDetails(@JsonProperty("carrierCode") String carrierCode,
                             @JsonProperty("number") String number,
                             @JsonProperty("departureTime") String departureTime,
                             @JsonProperty("arrivalTime") String arrivalTime) {
            this.carrierCode = carrierCode;
            this.number = number;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }
    }
}
