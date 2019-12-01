package pw.peterwhite.flights.clients;

/**
 * <b>Client</b>: Abstract class that is used to consume APIs of format "baseUrl/{endpoint}".
 */
public abstract class Client  {
    protected String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
