package org.GT659010.OrderHandling.OrderTypes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

@JsonTypeName("limit")
public class LimitOrder extends Order {
    private final int limitPrice;

    public LimitOrder(String userId, Side side, int size, int limitPrice) {
        super(userId, side, size);
        this.limitPrice = limitPrice;
    }

    public int getLimitPrice() { return limitPrice; }

    @Override
    public String toString() {
        return "Limit" + super.toString() + "[Price=" + limitPrice + "]";
    }
}
