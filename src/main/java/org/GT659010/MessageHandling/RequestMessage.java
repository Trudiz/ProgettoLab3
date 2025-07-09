package org.GT659010.MessageHandling;

public class RequestMessage {
    private String operation;
    private Payload payload;

    public RequestMessage() {}
    public RequestMessage(String operation, Payload payload) {
        this.operation = operation;
        this.payload = payload;
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
