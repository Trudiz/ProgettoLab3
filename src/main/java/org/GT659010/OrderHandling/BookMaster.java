package org.GT659010.OrderHandling;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.GT659010.OrderHandling.OrderTypes.LimitOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            // Carica gli ordini di acquisto (Bids)
            if (Files.exists(ACTIVEBIDS) && Files.size(ACTIVEBIDS) > 0) {
                List<LimitOrder> loadedBids = ORDERMAPPER.readValue(ACTIVEBIDS.toFile(),
                        new TypeReference<List<LimitOrder>>() {});

                // Aggiunge tutti gli ordini caricati alla coda dei bids
                // addAll è efficiente per le PriorityQueue
                orderBook.getBids().addAll(loadedBids);
                System.out.println("Loaded " + loadedBids.size() + " active bids.");
            }

            // Carica gli ordini di vendita (Asks)
            if (Files.exists(ACTIVEASKS) && Files.size(ACTIVEASKS) > 0) {
                List<LimitOrder> loadedAsks = ORDERMAPPER.readValue(ACTIVEASKS.toFile(),
                        new TypeReference<List<LimitOrder>>() {});

                // Aggiunge tutti gli ordini caricati alla coda degli asks
                orderBook.getAsks().addAll(loadedAsks);
                System.out.println("Loaded " + loadedAsks.size() + " active asks.");
            }

        } catch (Exception e) {
            System.err.println("Error loading active orders from file.");
            e.printStackTrace();
        }
    }

    /**
     * Salva lo stato corrente delle code di Bids e Asks su file JSON.
     * @param orderBook L'istanza dell'order book da cui salvare gli ordini.
     */
    public static void saveActiveOrders(OrderBook orderBook) {
        try {
            System.out.println("Saving active orders to disk...");
            // Salva i bids
            ORDERMAPPER.writeValue(ACTIVEBIDS.toFile(), orderBook.getBids());
            // Salva gli asks
            ORDERMAPPER.writeValue(ACTIVEASKS.toFile(), orderBook.getAsks());
            System.out.println("Active orders saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving active orders to file.");
            e.printStackTrace();
        }
    }

    public static int getHighestOrderIdFromHistory() {
        int maxId = 0;

        // Usiamo JsonFactory per creare un parser efficiente
        JsonFactory factory = new JsonFactory();

        // try-with-resources garantisce che il parser venga chiuso correttamente
        try (JsonParser parser = factory.createParser(STORICO_ORDINI.toFile())) {

            // Se il file non inizia con un array '[', esci
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                return 0;
            }

            // Itera su tutti gli elementi dell'array
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                String fieldName = parser.getCurrentName();

                // Se troviamo il campo "orderId"
                if ("orderId".equals(fieldName)) {
                    parser.nextToken(); // Muoviti al valore del campo
                    int currentId = parser.getIntValue(); // Leggi il valore intero

                    // Aggiorna il massimo se necessario
                    if (currentId > maxId) {
                        maxId = currentId;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura dello storico ordini: " + e.getMessage());
        }

        System.out.println("ID massimo trovato nello storico: " + maxId);
        return maxId;
    }
}
