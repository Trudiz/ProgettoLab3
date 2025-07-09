package org.GT659010.MessageHandling.RequestMessages;

import org.GT659010.MessageHandling.Payload;

public class PriceHistoryPayload implements Payload {
    private String month;

    public PriceHistoryPayload() {}

    public PriceHistoryPayload(String month) {
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
