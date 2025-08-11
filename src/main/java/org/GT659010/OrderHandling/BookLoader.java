package org.GT659010.OrderHandling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.GT659010.MessageHandling.PriceResponseMessage;
import org.GT659010.OrderHandling.OrderTypes.LimitOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookLoader {
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

    public static List<PriceResponseMessage> getDailyOHLCForMonth(String monthString) {
        // 1. Validazione (invariata)
        YearMonth targetMonth;
        try {
            targetMonth = YearMonth.parse(monthString);
        } catch (DateTimeParseException e) {
            System.err.println("Formato mese non valido: " + monthString);
            return new ArrayList<>();
        }

        // 2. Lettura del file (invariata)
        List<HistoricalRecord> allRecords;
        if (!Files.exists(STORICO_ORDINI)) return new ArrayList<>();
        try {
            allRecords = ORDERMAPPER.readValue(STORICO_ORDINI.toFile(), StoricoOrdiniFile.class).getOrderList();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        System.out.println("DEBUG: Trovati " + allRecords.size() + " record totali nel file storico.");
        System.out.println("DEBUG: Sto cercando corrispondenze per il mese: " + targetMonth);

        // 3. Filtra i record per il mese giusto (CON STAMPE DI DEBUG)
        List<HistoricalRecord> monthlyRecords = allRecords.stream()
                .peek(record -> {
                    // --- INIZIO BLOCCO DI DEBUG ---
                    // Per ogni record, calcoliamo il suo mese e lo stampiamo
                    Instant recordInstant = Instant.ofEpochSecond(record.getTimestamp());
                    YearMonth recordMonth = YearMonth.from(recordInstant.atZone(ZoneId.of("GMT")));

                    // Stampa il confronto che sta per essere fatto
                    System.out.println(
                            "DEBUG: Controllo Record ID " + record.getOrderId() +
                                    " -> Mese del record: " + recordMonth +
                                    " | Mese cercato: " + targetMonth +
                                    " | Corrisponde? -> " + recordMonth.equals(targetMonth)
                    );
                    // --- FINE BLOCCO DI DEBUG ---
                })
                .filter(record ->
                        YearMonth.from(Instant.ofEpochSecond(record.getTimestamp()).atZone(ZoneId.of("GMT")))
                                .equals(targetMonth)
                )
                .collect(Collectors.toList());

        System.out.println("DEBUG: Trovati " + monthlyRecords.size() + " record dopo il filtro.");

        // Se il problema è nel filtro, monthlyRecords.size() sarà 0.

        if (monthlyRecords.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. ORDINA TUTTI I RECORD PER DATA
        monthlyRecords.sort(Comparator.comparing(HistoricalRecord::getTimestamp));

        // 4. SCORRI LA LISTA ORDINATA E CALCOLA OHLC "A BLOCCHI"
        List<PriceResponseMessage> resultList = new ArrayList<>();
        // Lista temporanea per i record del giorno che stiamo analizzando
        List<HistoricalRecord> recordsForCurrentDay = new ArrayList<>();

        for (HistoricalRecord record : monthlyRecords) {
            if (recordsForCurrentDay.isEmpty() || isSameDay(recordsForCurrentDay.get(0), record)) {
                // Se la lista del giorno è vuota o se questo record è dello stesso giorno, aggiungilo
                recordsForCurrentDay.add(record);
            } else {
                // Se il giorno è cambiato, calcola l'OHLC per il blocco appena concluso
                resultList.add(calculateOHLCForDay(recordsForCurrentDay));

                // Pulisci la lista e inizia il nuovo blocco con il record attuale
                recordsForCurrentDay.clear();
                recordsForCurrentDay.add(record);
            }
        }

        // IMPORTANTE: Calcola l'OHLC per l'ultimo blocco di giorni rimasto nella lista
        if (!recordsForCurrentDay.isEmpty()) {
            resultList.add(calculateOHLCForDay(recordsForCurrentDay));
        }

        return resultList;
    }

    /** Metodo di supporto per calcolare l'OHLC di una lista di record giornalieri */
    private static PriceResponseMessage calculateOHLCForDay(List<HistoricalRecord> dailyRecords) {
        LocalDate day = Instant.ofEpochSecond(dailyRecords.get(0).getTimestamp())
                .atZone(ZoneId.of("GMT"))
                .toLocalDate();

        int openPrice = dailyRecords.get(0).getPrice(); // Il primo della lista ordinata
        int closePrice = dailyRecords.get(dailyRecords.size() - 1).getPrice(); // L'ultimo
        int highPrice = dailyRecords.stream().mapToInt(HistoricalRecord::getPrice).max().getAsInt();
        int lowPrice = dailyRecords.stream().mapToInt(HistoricalRecord::getPrice).min().getAsInt();

        return new PriceResponseMessage(day.toString(), openPrice, closePrice, highPrice, lowPrice);
    }

    /** Metodo di supporto per confrontare se due record appartengono allo stesso giorno */
    private static boolean isSameDay(HistoricalRecord record1, HistoricalRecord record2) {
        LocalDate day1 = Instant.ofEpochSecond(record1.getTimestamp()).atZone(ZoneId.of("GMT")).toLocalDate();
        LocalDate day2 = Instant.ofEpochSecond(record2.getTimestamp()).atZone(ZoneId.of("GMT")).toLocalDate();
        return day1.equals(day2);
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
