package org.GT659010.OrderHandling.OrderTypes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

// Nel file LimitOrder.java

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.GT659010.OrderHandling.Order;
import org.GT659010.OrderHandling.Side;

public class LimitOrder extends Order {
    private final int limitPrice;

    /**
     * ANNOTAZIONE CHIAVE: Questo costruttore ora dice a Jackson come creare un LimitOrder.
     * Ogni parametro corrisponde a una proprietà nel file JSON.
     */
    @JsonCreator
    public LimitOrder(@JsonProperty("orderId") int orderId,
                      @JsonProperty("userId") String userId,
                      @JsonProperty("type") Side side,
                      @JsonProperty("size") int size,
                      @JsonProperty("timestamp") long timestampSeconds,
                      @JsonProperty("limitPrice") int limitPrice) { // Campo specifico di LimitOrder

        // Chiama il costruttore della classe base per impostare i campi comuni
        super(orderId, userId, side, size, timestampSeconds);

        // Imposta il campo specifico di questa classe
        this.limitPrice = limitPrice;
    }

    /**
     * Questo è un secondo costruttore, utile per creare NUOVI ordini dal tuo codice,
     * dove l'ID viene generato automaticamente.
     */
    public LimitOrder(String userId, Side side, int size, int limitPrice) {
        super(userId, side, size); // Chiama il costruttore di Order che genera un nuovo ID
        this.limitPrice = limitPrice;
    }


    public int getLimitPrice() {
        return limitPrice;
    }

    @Override
    public String toString() {
        // Implementazione del toString se necessario
        return super.toString().replace("Order", "LimitOrder") + " [Price=" + limitPrice + "]";
    }
}
