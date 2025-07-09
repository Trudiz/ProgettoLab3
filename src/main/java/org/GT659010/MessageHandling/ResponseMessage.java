package org.GT659010.MessageHandling;

public class ResponseMessage {
    private int response;
    private String errorMessage;

    public ResponseMessage() {
        this.response = 103;
        this.errorMessage = "Unknown error!";
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
}
