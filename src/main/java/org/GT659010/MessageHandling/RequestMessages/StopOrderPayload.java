package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;
import org.GT659010.OrderHandling.Side;

public class StopOrderPayload implements Payload {
    private Side type;
    private int size;
    private int price;

    public StopOrderPayload() {}

    public StopOrderPayload(Side type, int size, int price) {
        this.type = type;
        this.size = size;
        this.price = price;
    }

    public Side getType() {
        return type;
    }

    public void setType(Side type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
