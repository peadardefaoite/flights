package pw.peterwhite.flights.clients;

import java.net.URI;

/**
 * <b>Client</b>: Abstract class that is used to consume APIs of format "baseUrl/{endpoint}".
 */
public abstract class Client  {
    protected String baseUrl;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
