package com.aystream.smsw.core.trade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TradeBroadcastService {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TradeBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized void broadcastTrade(Trade trade) {
        log.info("Broadcasting trade: " + trade);
        messagingTemplate.convertAndSend("/topic/trades", trade);
    }
}