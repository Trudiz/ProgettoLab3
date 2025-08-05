package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;
import org.GT659010.OrderHandling.Side;

public class MarketOrderPayload implements Payload {
    private Side type;
    private int size;

    public MarketOrderPayload() {}

    public MarketOrderPayload(Side type, int size) {
        this.type = type;
        this.size = size;
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
}
