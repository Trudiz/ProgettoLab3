package org.GT659010.MessageHandling.RequestMessages;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.GT659010.MessageHandling.Payload;

import java.time.YearMonth;

public class PriceHistoryPayload implements Payload {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
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
