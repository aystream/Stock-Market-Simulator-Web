package com.aystream.smsw.core.trade;

import com.aystream.smsw.core.orderbook.Order;
import com.aystream.smsw.core.orderbook.OrderBook;
import com.aystream.smsw.core.orderbook.OrderBookManager;
import com.aystream.smsw.core.orderbook.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderBookConcurrencyTest {
    @Autowired
    private OrderBookManager orderBookManager;
    private static final int NUMBER_OF_THREADS = 10;
    private static final String SYMBOL = "AAPL";
    private ExecutorService executorService;

    @BeforeEach
    public void setup() {
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    @Test
    public void testConcurrentOrderHandling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Future<?> future = executorService.submit(() -> {
                try {
                    OrderBook orderBook = orderBookManager.getOrderBook(SYMBOL);
                    Order order = createRandomOrder();
                    orderBook.addOrder(order);
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        latch.await(); // Wait for all threads to finish

        // Verify the state of the order book
        OrderBook orderBook = orderBookManager.getOrderBook(SYMBOL);
        assertNotNull(orderBook, "Order book should not be null");

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                fail("Exception during concurrent execution: " + e.getCause());
            }
        }
    }

    private Order createRandomOrder() {
        // Implement logic to create a random order (either BUY or SELL)
        Order order = new Order();
        order.setType(Math.random() < 0.5 ? OrderType.BUY : OrderType.SELL);
        order.setSymbol(SYMBOL);
        order.setPrice((int) (Math.random() * 100) + 1); // Random price between 1 and 100
        order.setQuantity((int) (Math.random() * 100) + 1); // Random quantity between 1 and 100
        return order;
    }
}
