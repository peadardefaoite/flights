package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for the response returned from Schedules API
 * Only populated with values that are necessary for our microservice
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {
    private int month;
    private List<DayFlights> days;

    public Schedule() {
        // For testing only
    }

    @JsonCreator
    public Schedule(@JsonProperty("month") int month,
                    @JsonProperty("days") List<DayFlights> days) {
        this.month = month;
        this.days = days;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDays(List<DayFlights> days) {
        this.days = days;
    }

    public List<DayFlights> getDays() {
        return days;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "month=" + month +
                ", days=" + days +
                '}';
    }

    public static class DayFlights {

        private int day;
        private List<FlightDetails> flights;

        public DayFlights(@JsonProperty("day") int day,
                          @JsonProperty("flights") List<FlightDetails> flights) {
            this.day = day;
            this.flights = flights;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public List<FlightDetails> getFlights() {
            return flights;
        }

        public void setFlights(List<FlightDetails> flights) {
            this.flights = flights;
        }

        @Override
        public String toString() {
            return "DayFlights{" +
                    "day=" + day +
                    ", flights=" + flights +
                    '}';
        }

    }
    public static class FlightDetails {
        private String departureTime;
        private String arrivalTime;

        public FlightDetails(@JsonProperty("departureTime") String departureTime,
                             @JsonProperty("arrivalTime") String arrivalTime) {
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        @Override
        public String toString() {
            return "FlightDetails{" +
                    "departureTime='" + departureTime + '\'' +
                    ", arrivalTime='" + arrivalTime + '\'' +
                    '}';
        }

    }
}
