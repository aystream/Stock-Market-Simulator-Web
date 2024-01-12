package com.aystream.smsw.core.trade;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradeLedger {
    private final TradeRepository tradeRepository;

    public TradeLedger(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void recordTrade(Trade trade) {
        tradeRepository.save(trade);
    }

    public List<Trade> getRecentTrades() {
        return tradeRepository.findAll(); // TODO better to take only batch
    }
}