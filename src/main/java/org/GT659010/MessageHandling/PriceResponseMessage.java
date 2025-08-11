package org.GT659010.MessageHandling;

public class PriceResponseMessage {
    private String date;
    private int open;
    private int close;
    private int high;
    private int low;

    public PriceResponseMessage(String date, int open, int close, int high, int low) {
        this.date = date;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getClose() {
        return close;
    }

    public void setClose(int close) {
        this.close = close;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getLow() {
        return low;
    }
    public void setLow(int low) {
        this.low = low;
    }
}
