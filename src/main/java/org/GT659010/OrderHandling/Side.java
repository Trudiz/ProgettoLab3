package org.GT659010.OrderHandling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Side {
    BID("bid"),
    ASK("ask");

    private final String value;

    Side(String value) {
        this.value = value;
    }

    // Dice a Jackson di usare questo valore quando scrive il JSON
    @JsonValue
    public String getValue() {
        return value;
    }

    // Dice a Jackson di usare questo metodo per creare l'enum dal JSON
    @JsonCreator
    public static Side fromValue(String value) {
        for (Side side : Side.values()) {
            if (side.value.equalsIgnoreCase(value)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Unknown side: " + value);
    }
}
