package org.GT659010.OrderHandling.OrderTypes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

@JsonTypeName("stop")
public class StopOrder extends Order {
    private final int stopPrice;

    public StopOrder(String userId, Side side, int size, int stopPrice) {
        super(userId, side, size);
        this.stopPrice = stopPrice;
    }

    public int getStopPrice() { return stopPrice; }
    @Override
    public String toString() {
        return "Stop" + super.toString() + "[StopPrice=" + stopPrice + "]";
    }
}
