package com.aystream.smsw.core.trade;


import com.aystream.smsw.core.orderbook.Order;
import com.aystream.smsw.core.orderbook.OrderBook;
import com.aystream.smsw.core.orderbook.OrderStatus;
import com.aystream.smsw.core.orderbook.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {
    private OrderBook orderBook;
    @BeforeEach
    public void setup() {
        orderBook = new OrderBook("AAPL");
    }

    @Test
    public void testAddAndMatchOrders() {
        // Add a buy order
        Order buyOrder = createOrder(1L, OrderType.BUY,"AAPL", 100, 10);
        orderBook.addOrder(buyOrder);

        // Add a sell order
        Order sellOrder = createOrder(2L, OrderType.SELL, "AAPL", 100, 10);
        orderBook.addOrder(sellOrder);

        // Match orders
        List<Trade> trades = orderBook.matchOrders();

        // Assertions
        assertEquals(1, trades.size(), "There should be one trade executed");
        Trade trade = trades.get(0);
        assertEquals(10, trade.getQuantity(), "Trade quantity should match the order quantity");
        assertEquals(100, trade.getPrice(), "Trade price should match the sell order price");
    }

    @Test
    public void testRemoveOrder() {
        Order order = createOrder(1L, OrderType.BUY, "GOOG", 150, 15);
        orderBook.addOrder(order);
        boolean removed = orderBook.removeOrder(order);

        assertTrue(removed, "Order should be removed successfully");
        assertFalse(orderBook.containsOrder(order.getId()), "Order book should not contain the removed order");
    }
    private Order createOrder(long id, OrderType type, String symbol, int price, int quantity) {
        Order order = new Order();
        order.setId(id);
        order.setType(type);
        order.setStatus(OrderStatus.PENDING);
        order.setSymbol(symbol);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setTimestamp(new Date());
        return order;
    }
}