package org.GT659010.OrderHandling;

import org.GT659010.MessageHandling.PriceResponseMessage;
import org.GT659010.OrderHandling.OrderTypes.LimitOrder;
import org.GT659010.OrderHandling.OrderTypes.MarketOrder;
import org.GT659010.OrderHandling.OrderTypes.StopOrder;
import org.GT659010.ServerMain;
import org.GT659010.UserHandling.User;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class OrderBook {
    private Integer lastTradedPrice;
    private final List<HistoricalRecord> historicalOrders;

    private final Lock lock = new ReentrantLock();

    public OrderBook(List<HistoricalRecord> historicalOrders) {
        this.historicalOrders = historicalOrders; // <-- SALVA IL RIFERIMENTO
    }

    // Bids -> Ordini di acquisto (BUY). Prezzo più alto ha la priorità.
    private final PriorityQueue<LimitOrder> bids = new PriorityQueue<>((o1, o2) -> {
        int priceComparison = Integer.compare(o2.getLimitPrice(), o1.getLimitPrice());
        if (priceComparison == 0) return o1.getTimestamp().compareTo(o2.getTimestamp());
        return priceComparison;
    });

    // Asks -> Ordini di vendita (SELL). Prezzo più basso ha la priorità.
    private final PriorityQueue<LimitOrder> asks = new PriorityQueue<>((o1, o2) -> {
        int priceComparison = Integer.compare(o1.getLimitPrice(), o2.getLimitPrice());
        if (priceComparison == 0) return o1.getTimestamp().compareTo(o2.getTimestamp());
        return priceComparison;
    });

    private final List<StopOrder> stopOrders = new ArrayList<>();
    private final List<Trade> tradeHistory = new ArrayList<>();

    public void addNewOrder(Order order) {
        lock.lock();
        try {
            System.out.println("--> Adding new order: " + order);
            if (order instanceof LimitOrder) {
                processLimitOrder((LimitOrder) order);
            } else if (order instanceof MarketOrder) {
                processMarketOrder((MarketOrder) order);
            } else if (order instanceof StopOrder) {
                stopOrders.add((StopOrder) order);
            }
            checkStopOrders();
        } finally {
            lock.unlock();
        }
    }

    private void processLimitOrder(LimitOrder order) {
        // CORREZIONE: I BUY vanno nei BIDS, i SELL negli ASKS.
        if (order.getSide() == Side.BUY) {
            bids.add(order);
        } else { // SELL
            asks.add(order);
        }
        tryMatch();
    }

    private void processMarketOrder(MarketOrder order) {
        List<Trade> trades = new ArrayList<>();
        // CORREZIONE: Un ordine BUY matcha contro gli ASKS.
        if (order.getSide() == Side.BUY) {
            while (order.getRemainingSize() > 0 && !asks.isEmpty()) {
                executeTrade(order, asks.peek(), asks.peek().getLimitPrice(), trades);
            }
        } else { // Un ordine SELL matcha contro i BIDS.
            while (order.getRemainingSize() > 0 && !bids.isEmpty()) {
                executeTrade(order, bids.peek(), bids.peek().getLimitPrice(), trades);
            }
        }
        trades.forEach(System.out::println);
        tradeHistory.addAll(trades);
    }

    public boolean processCancelOrder(int orderIdToCancel) {
        lock.lock();
        try {
            // 1. Prova a rimuovere l'ordine dalla coda dei bids (acquisti)
            boolean cancelled = bids.removeIf(order -> order.getOrderId() == orderIdToCancel);

            // 2. Se non è stato trovato nei bids, prova con gli asks (vendite)
            if (!cancelled) {
                cancelled = asks.removeIf(order -> order.getOrderId() == orderIdToCancel);
            }
            // 3. Se ancora non trovato, prova tra gli ordini stop pendenti
            if (!cancelled) {
                cancelled = stopOrders.removeIf(order -> order.getOrderId() == orderIdToCancel);
            }
            // 4. Fornisci un feedback sull'esito dell'operazione
            if (cancelled) {
                System.out.println("Ordine " + orderIdToCancel + " cancellato con successo.");
                return true;
            } else {
                System.out.println("Impossibile cancellare l'ordine " + orderIdToCancel + ": non trovato o già eseguito.");
                return false;
            }
        } finally  {
            lock.unlock();
        }
    }

    private void tryMatch() {
        List<Trade> trades = new ArrayList<>();
        // La condizione di match è quando il bid più alto è >= all'ask più basso
        while (!bids.isEmpty() && !asks.isEmpty() && bids.peek().getLimitPrice() >= asks.peek().getLimitPrice()) {
            LimitOrder bestBid = bids.peek();
            LimitOrder bestAsk = asks.peek();
            int tradePrice = bestBid.getTimestamp().isBefore(bestAsk.getTimestamp()) ?
                    bestBid.getLimitPrice() : bestAsk.getLimitPrice();
            executeTrade(bestBid, bestAsk, tradePrice, trades);
        }
        trades.forEach(System.out::println);
        tradeHistory.addAll(trades);
    }

    private void executeTrade(Order aggressor, LimitOrder bookOrder, int tradePrice, List<Trade> trades) {
        int tradeSize = Math.min(aggressor.getRemainingSize(), bookOrder.getRemainingSize());
        aggressor.decreaseRemainingSize(tradeSize);
        bookOrder.decreaseRemainingSize(tradeSize);
        this.lastTradedPrice = tradePrice;

        // CORREZIONE: ID utente come String e logica di assegnazione buyer/seller corretta.
        String buyerId;
        String sellerId;
        if (aggressor.getSide() == Side.BUY) {
            buyerId = aggressor.getUserId();
            sellerId = bookOrder.getUserId();
        } else { // Aggressor is a SELL
            buyerId = bookOrder.getUserId();
            sellerId = aggressor.getUserId();
        }

        // Calcola il costo totale in millesimi di USD
        int totalCost = tradeSize * tradePrice / 1000; // Dividi per 1000 perché entrambi sono in millesimi

        // Recupera gli oggetti User dalla mappa
        User buyer = ServerMain.USERS.get(buyerId);
        User seller = ServerMain.USERS.get(sellerId);

        // Controlla che gli utenti esistano prima di procedere
        if (buyer != null && seller != null) {
            // Aggiorna i saldi (questi metodi dovrai crearli nella tua classe User)
            buyer.decreaseUSDbalance(totalCost);
            buyer.increaseBTCbalance(tradeSize);

            seller.increaseUSDbalance(totalCost);
            seller.decreaseBTCbalance(tradeSize);

            System.out.println("Saldi aggiornati per " + buyerId + " e " + sellerId);

        } else {
            System.err.println("ERRORE CRITICO: Utente non trovato durante l'esecuzione del trade. Buyer: " + buyerId + ", Seller: " + sellerId);
            // Poi gestire errori vari
        }

        trades.add(new Trade(tradePrice, tradeSize, buyerId, sellerId));

        // CORREZIONE: Rimuovi l'ordine dalla coda corretta.
        if (bookOrder.isFilled()) {
            if (bookOrder.getSide() == Side.BUY) {
                bids.poll();
            } else { // SELL
                asks.poll();
            }
        }
    }

    private void checkStopOrders() {
        if (lastTradedPrice == null) return;
        List<StopOrder> triggeredStopOrders = new ArrayList<>();
        for (StopOrder stopOrder : stopOrders) {
            boolean triggered = false;
            // CORREZIONE: Logica di attivazione stop corretta.
            // Un BUY Stop si attiva se il prezzo SALE fino allo stopPrice.
            if (stopOrder.getSide() == Side.BUY && lastTradedPrice >= stopOrder.getStopPrice()) {
                triggered = true;
            }
            // Un SELL Stop (stop-loss) si attiva se il prezzo SCENDE fino allo stopPrice.
            else if (stopOrder.getSide() == Side.SELL && lastTradedPrice <= stopOrder.getStopPrice()) {
                triggered = true;
            }

            if (triggered) {
                triggeredStopOrders.add(stopOrder);
                System.out.println("!!! STOP ORDER TRIGGERED: " + stopOrder);
            }
        }
        if (!triggeredStopOrders.isEmpty()) {
            stopOrders.removeAll(triggeredStopOrders);
            // Assumo che l'ordine stop diventi un MarketOrder
            triggeredStopOrders.forEach(triggeredOrder -> addNewOrder(new MarketOrder(
                    triggeredOrder.getUserId(), // I costruttori sono stati adattati
                    triggeredOrder.getSide(),
                    triggeredOrder.getOriginalSize())));
        }
    }

    // --- Metodi di utilità e visualizzazione (invariati ma inclusi per completezza) ---

    public PriorityQueue<LimitOrder> getBids() { return bids; }
    public PriorityQueue<LimitOrder> getAsks() { return asks; }


    public PriceResponseMessage getPriceHistory(YearMonth targetMonth) {
        lock.lock();
        try {
            // 1. Filtra tutti i trade per ottenere solo quelli del mese specificato.
            List<Trade> monthlyTrades = this.tradeHistory.stream()
                    .filter(trade -> {
                        // Converte il timestamp di ogni trade in YearMonth per il confronto.
                        // Usiamo ZoneId.systemDefault() per usare il fuso orario del server.
                        return YearMonth.from(trade.getTimestamp().atZone(ZoneId.systemDefault()))
                                .equals(targetMonth);
                    })
                    .collect(Collectors.toList());

            // 2. Se non ci sono trade per quel mese, ritorna una risposta con valori a zero.
            if (monthlyTrades.isEmpty()) {
                return new PriceResponseMessage(targetMonth.toString(), 0, 0, 0, 0);
            }

            // 3. Ordina i trade per data per trovare facilmente il prezzo di apertura e chiusura.
            monthlyTrades.sort(Comparator.comparing(Trade::getTimestamp));

            // 4. Calcola i valori OHLC.

            // Open: il prezzo del primo trade del mese.
            int openPrice = monthlyTrades.get(0).getPrice();

            // Close: il prezzo dell'ultimo trade del mese.
            int closePrice = monthlyTrades.get(monthlyTrades.size() - 1).getPrice();

            // High: il prezzo massimo nel flusso dei trade del mese.
            int highPrice = monthlyTrades.stream()
                    .mapToInt(Trade::getPrice)
                    .max()
                    .getAsInt(); // Sicuro perché la lista non è vuota.

            // Low: il prezzo minimo nel flusso dei trade del mese.
            int lowPrice = monthlyTrades.stream()
                    .mapToInt(Trade::getPrice)
                    .min()
                    .getAsInt();

            // 5. Costruisce e ritorna l'oggetto di risposta con i dati calcolati.
            return new PriceResponseMessage(targetMonth.toString(), openPrice, closePrice, highPrice, lowPrice);

        } finally {
            // 6. Rilascia sempre il lock, anche in caso di eccezioni.
            lock.unlock();
        }
    }


    public void printOrderBook() {
        lock.lock();
        try {
            System.out.println("\n================ BTC/USD ORDER BOOK ================");
            System.out.println("ASKS (Sell Orders):");
            List<LimitOrder> sortedAsks = new ArrayList<>(asks);
            sortedAsks.sort(Comparator.comparing(LimitOrder::getLimitPrice)); // Prezzo crescente per asks
            sortedAsks.forEach(System.out::println);
            System.out.println("--------------------------------------------------");
            System.out.println("BIDS (Buy Orders):");
            List<LimitOrder> sortedBids = new ArrayList<>(bids);
            sortedBids.sort(Comparator.comparing(LimitOrder::getLimitPrice).reversed()); // Prezzo decrescente per bids
            sortedBids.forEach(System.out::println);
            System.out.println("==================================================\n");
        } finally {
            lock.unlock();
        }
    }
}
