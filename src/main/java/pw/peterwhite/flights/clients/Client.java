package pw.peterwhite.flights.clients;

import java.net.URI;

/**
 * <b>Client</b>: Abstract class that is extended and used to consume.
 */
public abstract class Client  {
    protected String baseUrl;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
