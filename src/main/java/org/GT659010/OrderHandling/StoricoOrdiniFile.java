package org.GT659010.OrderHandling;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class StoricoOrdiniFile {
    @JsonProperty("trades") // Corrisponde alla chiave nel tuo file JSON
    private List<HistoricalRecord> orderList;

    public StoricoOrdiniFile() {
        this.orderList = new ArrayList<>();
    }

    // Getters e Setters necessari per Jackson
    public List<HistoricalRecord> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<HistoricalRecord> orderList) {
        this.orderList = orderList;
    }
}
