package org.GT659010.OrderHandling;

import java.time.Instant;

public abstract class Order {
    private final int orderId;
    private final int userId;
    private final Side side;
    private final int originalSize;
    private int remainingSize;
    private final Instant timestamp;

    public Order(int orderId, int userId, Side side, int size) {
        this.orderId = orderId;
        this.userId = userId;
        this.side = side;
        this.originalSize = size;
        this.remainingSize = size;
        this.timestamp = Instant.now();
    }

    // Getters
    public int getOrderId() { return orderId; }
    public int getUserId() { return userId; }
    public Side getSide() { return side; }
    public int getOriginalSize() { return originalSize; }
    public int getRemainingSize() { return remainingSize; }
    public Instant getTimestamp() { return timestamp; }

    public void decreaseRemainingSize(int amount) {
        this.remainingSize -= amount;
    }

    public boolean isFilled() {
        return remainingSize == 0;
    }

    @Override
    public String toString() {
        return "Order [ID=" + orderId + ", User=" + userId + ", Side=" + side + ", Size=" + originalSize + ", Rem=" + remainingSize + "]";
    }
}
