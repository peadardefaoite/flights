package pw.peterwhite.flights.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlightService implements Service {
    private static final Log logger = LogFactory.getLog(FlightService.class);

    public FlightService() {
    }

    public int getFlightInfo(){
        logger.info("getting flight info");
        return 0;
    }
}
