package com.aystream.smsw.cli;

import com.aystream.smsw.core.orderbook.Order;
import com.aystream.smsw.core.orderbook.OrderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Scanner;


@SpringBootApplication()
@Slf4j
public class CliApplication implements CommandLineRunner {
    public static final String ORDER_URI = "/orders";
    public static final String BASE_URL = "127.0.0.1:8080"; // TODO use file for const variables or
    private final WebClient webClient;
    private final WebSocketClient webSocketClient;
    private final String apiBaseUrl = "http://" + BASE_URL + "/api/v1/trading"; // TODO use in field in application.yml

    public CliApplication() {
        this.webClient = WebClient.create(apiBaseUrl);
        this.webSocketClient = new ReactorNettyWebSocketClient();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(CliApplication.class, args);

        Thread cliThread = new Thread(() -> {
            CliApplication cliApp = ctx.getBean(CliApplication.class);
            cliApp.startCLI();
        });

        // Start the CLI thread
        cliThread.start();
    }

    private void setupWebSocket() {
        URI uri = URI.create("ws://" + BASE_URL + "/ws");
        this.webSocketClient.execute(uri, session ->
                        session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .doOnNext(message -> log.info("Received message: " + message))
                                .then())
                .subscribe(null, error -> log.error("Error in WebSocket: " + error.getMessage()));
    }

    @Override
    public void run(String... args) {
        setupWebSocket();
    }

    public void startCLI() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break; // Exit loop if input stream is closed
                }
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    log.info("Exiting application...");
                    break;
                }
                processCommand(input);
            }
        } catch (Exception e) {
            log.error("An error occurred: " + e.getMessage());
        }
    }

    private void processCommand(String input) {
        String[] parts = input.split(" ");
        if (parts.length == 0) {
            log.info("Invalid command");
            return;
        }

        String command = parts[0].toLowerCase();
        switch (command) {
            case "add":
                handleAddCommand(parts);
                break;
            case "cancel":
                handleCancelCommand(parts);
                break;
            case "view":
                handleViewCommand(parts);
                break;
            default:
                log.info("Unknown command");
        }
    }

    private void handleAddCommand(String[] parts) {
        if (parts.length != 5) {
            log.info("Invalid add command format");
            return;
        }

        try {
            String symbol = parts[1];
            OrderType type = OrderType.valueOf(parts[2].toUpperCase());
            int quantity = Integer.parseInt(parts[3]);
            int price =  Integer.parseInt(parts[4]);

            Order order = new Order();
            order.setSymbol(symbol);
            order.setType(type);
            order.setQuantity(quantity);
            order.setPrice(price);
            order.setTimestamp(new Date());

            webClient.post()
                    .uri(ORDER_URI)
                    .bodyValue(order)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(aVoid -> log.info("[{}] Order added: {} {} {} @ {}", LocalDateTime.now(), symbol, type, quantity, price))
                    .doOnError(error -> log.error("Error adding order: ", error))
                    .subscribe();
        } catch (IllegalArgumentException e) {
            log.info("Error processing add command: " + e.getMessage());
        }
    }

    private void handleCancelCommand(String[] parts) {
        if (parts.length != 2) {
            log.info("Invalid cancel command format");
            return;
        }
        try {
            Long orderId = Long.parseLong(parts[1]);

            webClient.delete()
                    .uri(apiBaseUrl + ORDER_URI + "/" + orderId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(aVoid -> log.info("[{}] Order with ID {} cancelled", LocalDateTime.now(), orderId))
                    .doOnError(error -> log.error("Error adding order: ", error))
                    .subscribe();
        } catch (NumberFormatException e) {
            log.info("Invalid order ID");
        }
    }
    private void handleViewCommand(String[] parts) {
        if (parts.length != 2) {
            log.info("Invalid view command format");
            return;
        }
        String symbol = parts[1];

        webClient.get()
                .uri(apiBaseUrl + ORDER_URI + "?symbol=" + symbol)
                .retrieve()
                .bodyToFlux(Order.class)
                .collectList()
                .subscribe(orders -> {
                    if (orders.isEmpty()) {
                        log.info("No orders found for symbol: " + symbol);
                    } else {
                        orders.forEach(order -> log.info(formatOrder(order)));
                    }
                }, error -> log.error("Error fetching orders: " + error.getMessage()));
    }

    private String formatOrder(Order order) {
        return String.format("Order ID: %d, Type: %s, Symbol: %s, Quantity: %d, Price: %d, Status: %s",
                order.getId(), order.getType(), order.getSymbol(), order.getQuantity(), order.getPrice(), order.getStatus());
    }
}
