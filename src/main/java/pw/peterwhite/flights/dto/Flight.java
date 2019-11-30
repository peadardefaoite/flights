package pw.peterwhite.flights.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Flight {
    private int stops;
    private List<Leg> legs;

    public Flight(int stops, List<Leg> legs) {
        this.stops = stops;
        this.legs = legs;
    }
}
