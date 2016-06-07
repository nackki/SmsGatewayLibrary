package hippoping.smsgw.api.hybrid;

public abstract class HybridwsdlCallbackHandler {

    protected Object clientData;

    public HybridwsdlCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    public HybridwsdlCallbackHandler() {
        this.clientData = null;
    }

    public Object getClientData() {
        return this.clientData;
    }

    public void receiveResultsendMO(HybridwsdlStub.SendMOResponse result) {
    }

    public void receiveErrorsendMO(Exception e) {
    }
}