package org.GT659010.OrderHandling;

// Crea un nuovo file, es. HistoricalRecord.java

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricalRecord {
    private int orderId;
    private String type; // "bid" o "ask"
    private String orderType; // "limit" o "market"
    private int size;
    private int price;
    private long timestamp;

    public HistoricalRecord() {}

    // Costruttore per creare un record da un ordine eseguito
    public HistoricalRecord(int orderId, String type, String orderType, int size, int price, long timestamp) {
        this.orderId = orderId;
        this.type = type;
        this.orderType = orderType;
        this.size = size;
        this.price = price;
        this.timestamp = timestamp;
    }

    // Getters e Setters necessari per Jackson
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
