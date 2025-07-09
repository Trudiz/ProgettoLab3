package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;

public class CancelOrderPayload implements Payload {
    private int orderId;

    public CancelOrderPayload() {}

    public CancelOrderPayload(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
