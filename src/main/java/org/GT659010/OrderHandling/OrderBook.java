package org.GT659010.OrderHandling;

import org.GT659010.OrderHandling.OrderTypes.LimitOrder;
import org.GT659010.OrderHandling.OrderTypes.MarketOrder;
import org.GT659010.OrderHandling.OrderTypes.StopOrder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBook {
    private Integer lastTradedPrice;

    private final Lock lock = new ReentrantLock();

    private final PriorityQueue<LimitOrder> bids = new PriorityQueue<>((o1, o2) -> {
        int priceComparison = Integer.compare(o2.getLimitPrice(), o1.getLimitPrice());
        if (priceComparison == 0) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
        return priceComparison;
    });

    private final PriorityQueue<LimitOrder> asks = new PriorityQueue<>((o1, o2) -> {
        int priceComparison = Integer.compare(o1.getLimitPrice(), o2.getLimitPrice());
        if (priceComparison == 0) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
        return priceComparison;
    });

    private final List<StopOrder> stopOrders = new ArrayList<>();
    private final List<Trade> tradeHistory = new ArrayList<>();
    private final AtomicInteger orderIdGenerator = new AtomicInteger(1);

    public int getNextOrderId() {
        return orderIdGenerator.getAndIncrement();
    }

    public void addNewOrder(Order order) {
        lock.lock(); // Acquisisce il lock
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
            lock.unlock(); // Rilascia SEMPRE il lock nel blocco finally
        }
    }

    private void processLimitOrder(LimitOrder order) {
        if (order.getSide() == Side.ASK) bids.add(order);
        else asks.add(order);
        tryMatch();
    }

    private void processMarketOrder(MarketOrder order) {
        List<Trade> trades = new ArrayList<>();
        if (order.getSide() == Side.ASK) {
            while (order.getRemainingSize() > 0 && !asks.isEmpty()) {
                executeTrade(order, asks.peek(), asks.peek().getLimitPrice(), trades);
            }
        } else { // SELL
            while (order.getRemainingSize() > 0 && !bids.isEmpty()) {
                executeTrade(order, bids.peek(), bids.peek().getLimitPrice(), trades);
            }
        }
        trades.forEach(System.out::println);
        tradeHistory.addAll(trades);
    }

    private void tryMatch() {
        List<Trade> trades = new ArrayList<>();
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
        int buyerId = (aggressor.getSide() == Side.ASK) ? aggressor.getUserId() : bookOrder.getUserId();
        int sellerId = (aggressor.getSide() == Side.BID) ? aggressor.getUserId() : bookOrder.getUserId();
        trades.add(new Trade(tradePrice, tradeSize, buyerId, sellerId));
        if (bookOrder.isFilled()) {
            if (bookOrder.getSide() == Side.ASK) bids.poll();
            else asks.poll();
        }
    }

    private void checkStopOrders() {
        if (lastTradedPrice == null) return;
        List<StopOrder> triggeredStopOrders = new ArrayList<>();
        for (StopOrder stopOrder : stopOrders) {
            boolean triggered = false;
            if (stopOrder.getSide() == Side.ASK && lastTradedPrice >= stopOrder.getStopPrice()) {
                triggered = true;
            } else if (stopOrder.getSide() == Side.BID && lastTradedPrice <= stopOrder.getStopPrice()) {
                triggered = true;
            }
            if (triggered) {
                triggeredStopOrders.add(stopOrder);
                System.out.println("!!! STOP ORDER TRIGGERED: " + stopOrder);
            }
        }
        if (!triggeredStopOrders.isEmpty()) {
            stopOrders.removeAll(triggeredStopOrders);
            triggeredStopOrders.forEach(triggeredOrder -> addNewOrder(new MarketOrder(
                    triggeredOrder.getOrderId(), triggeredOrder.getUserId(),
                    triggeredOrder.getSide(), triggeredOrder.getOriginalSize())));
        }
    }

    public void printOrderBook() {
        lock.lock();
        try {
            System.out.println("\n================ BTC/USD ORDER BOOK ================");
            System.out.println("ASKS (Sell Orders):");
            List<LimitOrder> sortedAsks = new ArrayList<>(asks);
            sortedAsks.sort(Comparator.comparing(LimitOrder::getLimitPrice).reversed());
            sortedAsks.forEach(System.out::println);
            System.out.println("--------------------------------------------------");
            System.out.println("BIDS (Buy Orders):");
            List<LimitOrder> sortedBids = new ArrayList<>(bids);
            sortedBids.sort(Comparator.comparing(LimitOrder::getLimitPrice).reversed());
            sortedBids.forEach(System.out::println);
            System.out.println("==================================================\n");
        } finally {
            lock.unlock();
        }
    }
}
