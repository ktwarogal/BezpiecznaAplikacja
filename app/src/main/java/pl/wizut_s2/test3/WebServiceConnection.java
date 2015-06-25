package pl.wizut_s2.test3;

/**
 * Created by KrzaQ on 2015-06-25.
 */
public abstract class WebServiceConnection {

    private PBAIClientInterface pbaiClientInterface;

    public enum ServiceType {
        HTTP_GET,
        SOAP
    }

    abstract public ServiceType getType();

    protected WebServiceConnection(PBAIClientInterface pbaiClientInterface) {
        this.pbaiClientInterface = pbaiClientInterface;
    }

    protected final void onListOfScannersUpdate(String s) {
        pbaiClientInterface.onListOfScannersUpdate(s);
    }
}
