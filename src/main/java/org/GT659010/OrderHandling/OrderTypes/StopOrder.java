package org.GT659010.OrderHandling.OrderTypes;

import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

public class StopOrder extends Order {
    private final int stopPrice;

    public StopOrder(int orderId, int userId, Side side, int size, int stopPrice) {
        super(orderId, userId, side, size);
        this.stopPrice = stopPrice;
    }

    public int getStopPrice() { return stopPrice; }
    @Override
    public String toString() {
        return "Stop" + super.toString() + "[StopPrice=" + stopPrice + "]";
    }
}
