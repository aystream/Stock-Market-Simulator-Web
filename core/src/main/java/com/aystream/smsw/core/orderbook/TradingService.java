package com.aystream.smsw.core.orderbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TradingService {

    private final OrderService orderService;
    private final OrderBookManager orderBookManager;

    @Autowired
    public TradingService(OrderService orderService, OrderBookManager orderBookManager) {
        this.orderService = orderService;
        this.orderBookManager = orderBookManager;
    }

    @Transactional
    public Order addOrder(Order order) {
        OrderBook orderBook = orderBookManager.getOrderBook(order.getSymbol());
        order.setStatus(OrderStatus.PENDING);
        order.setTimestamp(new Date());
        Order savedOrder = orderService.saveOrder(order);
        orderBook.addOrder(savedOrder);

        return savedOrder;
    }

    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            OrderBook orderBook = orderBookManager.getOrderBook(order.getSymbol());
            boolean removed = orderBook.removeOrder(order);
            if (removed) {
                orderService.cancelOrder(order);
                return true;
            }
        }
        return false;
    }

    public List<Order> getOrdersBySymbol(String symbol) {
        return orderService.getOrdersBySymbol(symbol);
    }
}