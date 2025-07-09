package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;

public class MarketOrderPayload implements Payload {
    private String type;
    private double size;

    public MarketOrderPayload() {}

    public MarketOrderPayload(String type, double size) {
        this.type = type;
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
