package com.aystream.smsw.api.controller;

import com.aystream.smsw.core.exception.OrderNotFoundException;
import com.aystream.smsw.core.orderbook.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingGatewayController {
    private final TradingService tradingService;

    @Autowired
    public TradingGatewayController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> addOrder(@RequestBody Order order) {
        Order savedOrder = tradingService.addOrder(order);
        return ResponseEntity.ok(savedOrder);
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        boolean cancelled = tradingService.cancelOrder(orderId);
        if (!cancelled) {
            throw new OrderNotFoundException("Order not found for ID: " + orderId);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrdersBySymbol(@RequestParam String symbol) {
        List<Order> orders = tradingService.getOrdersBySymbol(symbol);
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders);
    }
}
