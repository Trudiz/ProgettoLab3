package org.GT659010.OrderHandling;

import java.time.Instant;

public class Trade {
    private final int price;
    private final int size;
    private final String buyerUserId;
    private final String sellerUserId;
    private final Instant timestamp;

    public Trade(int price, int size, String buyerUserId, String sellerUserId) {
        this.price = price;
        this.size = size;
        this.buyerUserId = buyerUserId;
        this.sellerUserId = sellerUserId;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return "TRADE EXECUTED: " + size + " @ " + price +
                " (Buyer: " + buyerUserId + ", Seller: " + sellerUserId + ") at " + timestamp;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getPrice() {
        return price;
    }
}
