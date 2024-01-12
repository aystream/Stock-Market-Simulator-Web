package com.aystream.smsw.api.controller;

import com.aystream.smsw.api.controller.handler.CustomStompSessionHandler;
import com.aystream.smsw.core.orderbook.Order;
import com.aystream.smsw.core.orderbook.OrderType;
import com.aystream.smsw.core.trade.Trade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private BlockingQueue<Trade> blockingQueue;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() throws Exception {
        blockingQueue = new LinkedBlockingQueue<>();
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String wsUrl = "ws://localhost:" + port + "/ws";
        stompSession = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
            // Override necessary methods
        }).get(5, TimeUnit.SECONDS);

        stompSession.subscribe("/topic/trades", new CustomStompSessionHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Trade.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((Trade) payload);
            }
        });
    }

    @Test
    public void testTradeBroadcast() throws Exception {
        simulateTrade();
        Trade receivedTrade = blockingQueue.poll(10, TimeUnit.SECONDS);
        assertNotNull(receivedTrade);
        stompSession.disconnect();
    }

    private void simulateTrade() throws URISyntaxException, InterruptedException {
        String baseUrl = "http://localhost:" + port + "/api/v1/trading/orders";
        URI uri = new URI(baseUrl);

        // Create a buy order
        Order buyOrder = new Order();
        buyOrder.setType(OrderType.BUY);
        buyOrder.setSymbol("AAPL");
        buyOrder.setPrice(100);
        buyOrder.setQuantity(10);

        // Send the buy order
        restTemplate.postForObject(uri, buyOrder, Order.class);

        // Create a sell order that matches the buy order
        Order sellOrder = new Order();
        sellOrder.setType(OrderType.SELL);
        sellOrder.setSymbol("AAPL");
        sellOrder.setPrice(100);
        sellOrder.setQuantity(10);

        // Send the sell order
        restTemplate.postForObject(uri, sellOrder, Order.class);
        Thread.sleep(4000);
    }

    @AfterEach
    public void tearDown() {
        stompClient.stop();
    }
}