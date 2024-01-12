package com.aystream.smsw.core.matching;

import com.aystream.smsw.core.orderbook.OrderBook;
import com.aystream.smsw.core.orderbook.OrderBookManager;
import com.aystream.smsw.core.orderbook.OrderService;
import com.aystream.smsw.core.trade.Trade;
import com.aystream.smsw.core.trade.TradeBroadcastService;
import com.aystream.smsw.core.trade.TradeLedger;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingEngine {
    private final OrderBookManager orderBookManager;
    private final TradeLedger tradeLedger;
    private final TradeBroadcastService tradeBroadcastService;
    private final OrderService orderService;

    public MatchingEngine(OrderBookManager orderBookManager,
                          TradeLedger tradeLedger,
                          TradeBroadcastService tradeBroadcastService, OrderService orderService) {
        this.orderBookManager = orderBookManager;
        this.tradeLedger = tradeLedger;
        this.tradeBroadcastService = tradeBroadcastService;
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 1000) // TODO change to variable in application.yml
    public void matchOrders() {
        orderBookManager.getAllOrderBooks().forEach(this::matchOrdersInBook);
    }

    private void matchOrdersInBook(OrderBook orderBook) {
        List<Trade> trades = orderBook.matchOrders();
        trades.forEach(this::processTrade);
    }

    @Transactional
    public void processTrade(Trade trade) {
        // Record and broadcast trade
        tradeLedger.recordTrade(trade);
        tradeBroadcastService.broadcastTrade(trade);
        // Persist changes in order statuses
        orderService.saveOrder(trade.getBuyOrder());
        orderService.saveOrder(trade.getSellOrder());
    }

}
