package org.GT659010.OrderHandling.OrderTypes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

@JsonTypeName("market")
public class MarketOrder extends Order {
    public MarketOrder(String userId, Side side, int size) {
        super(userId, side, size);
    }
    @Override
    public String toString() {
        return "Market" + super.toString();
    }
}
