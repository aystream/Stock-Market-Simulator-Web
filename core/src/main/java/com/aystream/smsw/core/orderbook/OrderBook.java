package com.aystream.smsw.core.orderbook;

import com.aystream.smsw.core.trade.Trade;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBook {
    private final String symbol;
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final Map<Long, Order> orderMap;
    private final ReentrantLock lock = new ReentrantLock();

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyOrders = new PriorityQueue<>(this::compareOrders);
        this.sellOrders = new PriorityQueue<>(this::compareOrders);
        this.orderMap = new HashMap<>();
    }

    private int compareOrders(Order o1, Order o2) {
        // For buy: higher prices and older timestamps have priority
        // For sell: lower prices and older timestamps have priority
        int priceComparison = o1.getType() == OrderType.BUY ? o2.getPrice().compareTo(o1.getPrice()) : o1.getPrice().compareTo(o2.getPrice());
        return (priceComparison != 0) ? priceComparison : o1.getTimestamp().compareTo(o2.getTimestamp());
    }

    public void addOrder(Order order) {
        lock.lock();
        try {
            PriorityQueue<Order> targetQueue = (order.getType() == OrderType.BUY) ? buyOrders : sellOrders;
            targetQueue.add(order);
            orderMap.put(order.getId(), order);
        } finally {
            lock.unlock();
        }
    }

    public List<Trade> matchOrders() {
        List<Trade> executedTrades = new ArrayList<>();
        lock.lock();
        try {
            while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
                Order buyOrder = buyOrders.peek();
                Order sellOrder = sellOrders.peek();

                if (buyOrder.getPrice() >= sellOrder.getPrice()) {
                    int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                    Trade trade = createTradeAndUpdateOrders(buyOrder, sellOrder, tradeQuantity);
                    executedTrades.add(trade);

                    if (buyOrder.getQuantity() == 0) {
                        buyOrders.poll();
                    }
                    if (sellOrder.getQuantity() == 0) {
                        sellOrders.poll();
                    }
                } else {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return executedTrades;
    }

    private Trade createTradeAndUpdateOrders(Order buyOrder, Order sellOrder, int tradeQuantity) {
        updateOrderQuantity(buyOrder, tradeQuantity);
        updateOrderQuantity(sellOrder, tradeQuantity);
        return createTrade(buyOrder, sellOrder, tradeQuantity);
    }

    private void updateOrderQuantity(Order order, int tradeQuantity) {
        order.setQuantity(order.getQuantity() - tradeQuantity);
        if (order.getQuantity() == 0) {
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
    }

    private Trade createTrade(Order buyOrder, Order sellOrder, int tradeQuantity) {
        Trade trade = new Trade();
        trade.setBuyOrder(buyOrder);
        trade.setSellOrder(sellOrder);
        trade.setSymbol(symbol);
        trade.setPrice(sellOrder.getPrice());
        trade.setQuantity(tradeQuantity);
        trade.setTimestamp(new Date());
        return trade;
    }

    public boolean removeOrder(Order order) {
        lock.lock();
        try {
            Order existingOrder = orderMap.get(order.getId());
            if (existingOrder == null) {
                return false;
            }
            PriorityQueue<Order> targetQueue = (existingOrder.getType() == OrderType.BUY) ? buyOrders : sellOrders;
            // Since PriorityQueue's remove operation is O(n), we directly remove from PriorityQueue using the existingOrder
            boolean removedFromQueue = targetQueue.remove(existingOrder);
            if (removedFromQueue) {
                orderMap.remove(existingOrder.getId());
            }
            return removedFromQueue;
        } finally {
            lock.unlock();
        }
    }

    public boolean containsOrder(Long orderId) {
        lock.lock();
        try {
            return orderMap.containsKey(orderId);
        } finally {
            lock.unlock();
        }
    }
}