package com.aystream.smsw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockMarketSimulatorWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockMarketSimulatorWebApplication.class, args);
    }
}