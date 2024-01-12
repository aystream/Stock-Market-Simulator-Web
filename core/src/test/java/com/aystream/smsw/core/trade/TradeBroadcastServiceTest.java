package com.aystream.smsw.core.trade;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.Mockito.*;

class TradeBroadcastServiceTest {
    @Test
    void testBroadcastTrade() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        TradeBroadcastService service = new TradeBroadcastService(mockTemplate);
        Trade trade = new Trade();
        service.broadcastTrade(trade);
        verify(mockTemplate).convertAndSend(eq("/topic/trades"), eq(trade));
    }
}