package com.mphasis.market.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketDataController {

    // In-memory mock data for asset prices
    private static final Map<String, BigDecimal> MOCK_PRICES = new ConcurrentHashMap<>();

    static {
        MOCK_PRICES.put("RNDR", new BigDecimal("12.34"));
        MOCK_PRICES.put("KNG", new BigDecimal("99.48"));
        MOCK_PRICES.put("SPY", new BigDecimal("450.76"));
        MOCK_PRICES.put("BTC", new BigDecimal("65102.00"));
    }

    /**
     * Internal endpoint used by the portfolio-service to fetch mock market prices.
     * 
     * @param symbol the stock or asset symbol
     * @return MarketPrice object containing the current (mocked) price
     */
    @GetMapping("/market/{symbol}")
    public MarketPrice getPrice(@PathVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();

        // Get base price or generate a random one if unknown
        BigDecimal basePrice = MOCK_PRICES.getOrDefault(
                upperSymbol,
                BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10, 500))
                        .setScale(2, RoundingMode.HALF_UP)
        );

        // Add a small random fluctuation to simulate live price movement
        BigDecimal fluctuation = BigDecimal
                .valueOf(ThreadLocalRandom.current().nextDouble(-0.5, 0.5))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = basePrice.add(fluctuation).max(BigDecimal.ZERO);

        return new MarketPrice(upperSymbol, finalPrice);
    }

    // Simple record to represent the response payload
    public record MarketPrice(String symbol, BigDecimal price) {}
}
