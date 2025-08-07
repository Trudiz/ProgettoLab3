package org.GT659010.OrderHandling;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.GT659010.OrderHandling.OrderTypes.LimitOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookMaster {
    // == File Paths ==
    private static final Path ACTIVEBIDS = Paths.get("activebids.json");
    private static final Path ACTIVEASKS = Paths.get("activeasks.json");
    private static final Path STORICO_ORDINI = Paths.get("storicoOrdini.json");

    // == Jackson Mapper ==
    // Aggiungi JavaTimeModule per gestire correttamente Instant
    private static final ObjectMapper ORDERMAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Carica gli ordini attivi dai file JSON e li inserisce nell'order book.
     * @param orderBook L'istanza dell'order book da popolare.
     */

    public static void loadActiveOrders(OrderBook orderBook) {
        try {
            // Crea un "lettore" specifico per una lista di LimitOrder.
            // Questo non cercherà il campo "orderType" perché sa già che tipo di oggetti creare.
            ObjectReader reader = ORDERMAPPER.readerFor(new TypeReference<List<LimitOrder>>() {});

            // Carica i bids
            if (Files.exists(ACTIVEBIDS) && Files.size(ACTIVEBIDS) > 0) {
                List<LimitOrder> loadedBids = reader.readValue(ACTIVEBIDS.toFile());
                orderBook.getBids().addAll(loadedBids);
                System.out.println("Caricati " + loadedBids.size() + " bids attivi.");
            }

            // Carica gli asks
            if (Files.exists(ACTIVEASKS) && Files.size(ACTIVEASKS) > 0) {
                List<LimitOrder> loadedAsks = reader.readValue(ACTIVEASKS.toFile());
                orderBook.getAsks().addAll(loadedAsks);
                System.out.println("Caricati " + loadedAsks.size() + " asks attivi.");
            }

        } catch (Exception e) {
            System.err.println("Errore durante il caricamento degli ordini attivi.");
            e.printStackTrace();
        }
    }

    /**
     * Salva lo stato corrente delle code di Bids e Asks su file JSON.
     * @param orderBook L'istanza dell'order book da cui salvare gli ordini.
     */
    public static void saveActiveOrders(OrderBook orderBook) {
        try {
            System.out.println("Salvataggio ordini attivi su disco...");

            // Crea uno "scrittore" specifico per una lista di LimitOrder.
            // Questo ignorerà le annotazioni di polimorfismo e non aggiungerà "orderType".
            ObjectWriter writer = ORDERMAPPER.writerFor(new TypeReference<List<LimitOrder>>() {});

            // Converte le code in liste
            List<LimitOrder> bidsToSave = new ArrayList<>(orderBook.getBids());
            List<LimitOrder> asksToSave = new ArrayList<>(orderBook.getAsks());

            // Usa lo scrittore specifico
            writer.writeValue(ACTIVEBIDS.toFile(), bidsToSave);
            writer.writeValue(ACTIVEASKS.toFile(), asksToSave);

            System.out.println("Ordini attivi salvati con successo.");
        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio degli ordini attivi.");
            e.printStackTrace();
        }
    }

    /**
     * Carica la lista di ordini storici dal file JSON.
     * @return Una lista di ordini; vuota se il file non esiste.
     */
    public static List<HistoricalRecord> loadHistory() {
        if (Files.exists(STORICO_ORDINI)) {
            try {
                StoricoOrdiniFile historyFile = ORDERMAPPER.readValue(STORICO_ORDINI.toFile(), StoricoOrdiniFile.class);
                System.out.println("Caricati " + historyFile.getOrderList().size() + " ordini dallo storico.");
                return historyFile.getOrderList();
            } catch (IOException e) {
                System.err.println("Errore durante la lettura dello storico ordini.");
                e.printStackTrace();
            }
        }
        System.out.println("File storico non trovato. Parto con una cronologia vuota.");
        return new ArrayList<>();
    }

    /**
     * Salva l'intera cronologia di ordini nel file JSON, sovrascrivendolo.
     * @param historicalOrders La lista completa degli ordini da salvare.
     */
    public static void saveHistory(List<HistoricalRecord> historicalOrders) {
        StoricoOrdiniFile historyFile = new StoricoOrdiniFile();
        historyFile.setOrderList(historicalOrders);

        try {
            System.out.println("Salvataggio di " + historicalOrders.size() + " ordini nello storico...");
            ORDERMAPPER.writeValue(STORICO_ORDINI.toFile(), historyFile);
            System.out.println("Storico ordini salvato.");
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio dello storico ordini.");
            e.printStackTrace();
        }
    }
}
