package org.GT659010.MessageHandling;

public class ResponseMessage {
    private int response;
    private String errorMessage;
    private Object payload;  // Il "contenitore" per i dati veri e propri

    public ResponseMessage() {
        this.response = 103;
        this.errorMessage = "Unknown error!";
        this.payload = null;
    }

    public int getResponse() {
        return this.response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
