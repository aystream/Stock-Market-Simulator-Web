package com.aystream.smsw.core.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TradeSerializationTest {
    @Test
    void testSerializationAndDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Trade trade = new Trade();
        String json = mapper.writeValueAsString(trade);
        Trade deserializedTrade = mapper.readValue(json, Trade.class);
        assertEquals(trade.getId(), deserializedTrade.getId());
    }
}