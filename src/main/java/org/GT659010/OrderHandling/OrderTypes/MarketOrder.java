package org.GT659010.OrderHandling.OrderTypes;

import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

public class MarketOrder extends Order {
    public MarketOrder(int orderId, int userId, Side side, int size) {
        super(orderId, userId, side, size);
    }
    @Override
    public String toString() {
        return "Market" + super.toString();
    }
}
