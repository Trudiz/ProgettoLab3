package org.GT659010.MessageHandling;

public class RequestMessage {
    private String operation;
    private Payload payload;

    public RequestMessage() {
        this.operation = "Invalid operation";
        this.payload = null;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
