package org.GT659010.OrderHandling;

import com.fasterxml.jackson.annotation.*;
import org.GT659010.OrderHandling.OrderTypes.LimitOrder;
import org.GT659010.OrderHandling.OrderTypes.MarketOrder;
import org.GT659010.OrderHandling.OrderTypes.StopOrder;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.GT659010.ServerMain.orderIdGenerator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "orderType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LimitOrder.class, name = "limit"),
        @JsonSubTypes.Type(value = MarketOrder.class, name = "market"),
        @JsonSubTypes.Type(value = StopOrder.class, name = "stop")
})
public abstract class Order {
    private final int orderId;
    private final String userId;
    @JsonProperty("orderType")
    private final Side side;
    private final int originalSize;
    private int remainingSize;

    @JsonIgnore // Jackson ignorerà questo campo direttamente
    private final Instant timestamp;

    /**
     * Questo è il costruttore che Jackson userà.
     * Prende il timestamp come numero (long) e lo converte in Instant.
     */
    @JsonCreator
    public Order(@JsonProperty("orderId") int orderId,
                 @JsonProperty("userId") String userId,
                 @JsonProperty("type") Side side,
                 @JsonProperty("size") int size,
                 @JsonProperty("timestamp") long timestampSeconds) {
        this.orderId = orderId;
        this.userId = userId;
        this.side = side;
        this.originalSize = size;
        this.remainingSize = size;
        this.timestamp = Instant.ofEpochSecond(timestampSeconds);
    }

    // Costruttore interno per creare ordini con Instant (se ti serve)
    public Order(String userId, Side side, int size) {
        this.orderId = orderIdGenerator.getAndIncrement();
        this.userId = userId;
        this.side = side;
        this.originalSize = size;
        this.remainingSize = size;
        this.timestamp = Instant.now(); // Usa il tempo attuale
    }
    // Getters
    public int getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public Side getSide() { return side; }
    public int getOriginalSize() { return originalSize; }
    public int getRemainingSize() { return remainingSize; }

    @JsonIgnore // Il getter standard di Instant viene ignorato
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Jackson userà QUESTO metodo per scrivere il timestamp nel JSON.
     * @return il timestamp come numero (long).
     */
    @JsonProperty("timestamp")
    public long getTimestampForJson() {
        if (timestamp == null) return 0;
        return timestamp.getEpochSecond();
    }

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
