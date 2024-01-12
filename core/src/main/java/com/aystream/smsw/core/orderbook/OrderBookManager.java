package com.aystream.smsw.core.orderbook;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderBookManager {
    private final ConcurrentHashMap<String, OrderBook> orderBooks;

    public OrderBookManager() {
        this.orderBooks = new ConcurrentHashMap<>();
    }

    public OrderBook getOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, this::createOrderBook);
    }

    private OrderBook createOrderBook(String symbol) {
        return new OrderBook(symbol);
    }

    public Collection<OrderBook> getAllOrderBooks() {
        return orderBooks.values();
    }
}
