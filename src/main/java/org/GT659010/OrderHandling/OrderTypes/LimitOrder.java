package org.GT659010.OrderHandling.OrderTypes;

import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

public class LimitOrder extends Order {
    private final int limitPrice;

    public LimitOrder(int orderId, int userId, Side side, int size, int limitPrice) {
        super(orderId, userId, side, size);
        this.limitPrice = limitPrice;
    }

    public int getLimitPrice() { return limitPrice; }

    @Override
    public String toString() {
        return "Limit" + super.toString() + "[Price=" + limitPrice + "]";
    }
}
