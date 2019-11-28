package pw.peterwhite.flights.config;

import pw.peterwhite.flights.services.FlightService;

public class FlightConfig {

    public FlightService flightService() {
        return new FlightService();
    }
}