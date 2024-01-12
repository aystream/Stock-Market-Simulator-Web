package com.aystream.smsw.api.controller;

import com.aystream.smsw.StockMarketSimulatorWebApplication;
import com.aystream.smsw.core.orderbook.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StockMarketSimulatorWebApplication.class)
@AutoConfigureMockMvc
public class TradingGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderBookManager orderBookManager;

    private Order testOrder;

    @BeforeEach
    public void setup() {
        testOrder = new Order();
        // Initialize the testOrder object with test data
        testOrder.setType(OrderType.BUY);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setSymbol("AAPL");
        testOrder.setPrice(100);
        testOrder.setQuantity(10);
        testOrder.setTimestamp(new Date());
        // Save the order to the repository if needed for the setup
        orderRepository.save(testOrder);
    }

    @Test
    public void whenAddOrder_thenOrderIsSavedAndAddedToOrderBook() throws Exception {
        String orderJson = objectMapper.writeValueAsString(testOrder);

        mockMvc.perform(post("/api/v1/trading/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify that the order is saved in the OrderBook
        OrderBook orderBook = orderBookManager.getOrderBook(testOrder.getSymbol());
        // Assert that the orderBook contains the order
        assertTrue(orderBook.containsOrder(testOrder.getId()));
    }

    @Test
    public void whenCancelOrder_thenOrderIsCancelledAndRemovedFromOrderBook() throws Exception {
        // First, add an order
        orderRepository.save(testOrder);
        OrderBook orderBook = orderBookManager.getOrderBook(testOrder.getSymbol());
        orderBook.addOrder(testOrder);

        // Now, cancel the order through the API
        mockMvc.perform(delete("/api/v1/trading/orders/{orderId}", testOrder.getId()))
                .andExpect(status().isOk());

        // Fetch the cancelled order from the repository
        Order cancelledOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        // Assert the order status is CANCELLED
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());

        // Verify that the order is removed from the OrderBook
        assertFalse(orderBook.containsOrder(testOrder.getId()));
    }
}