package org.GT659010.OrderHandling;

import org.GT659010.OrderHandling.OrderTypes.LimitOrder;
import org.GT659010.OrderHandling.OrderTypes.MarketOrder;
import org.GT659010.OrderHandling.OrderTypes.StopOrder;
import org.GT659010.ServerMain;
import org.GT659010.UserHandling.User;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
