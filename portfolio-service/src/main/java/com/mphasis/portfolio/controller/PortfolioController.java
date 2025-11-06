package com.mphasis.portfolio.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.mphasis.portfolio.model.*;

@RestController
public class PortfolioController {

    private final RestTemplate restTemplate;
    private final String marketDataServiceUrl;

    // Mock database of client portfolios
    private static final Map<String, List<Holding>> PORTFOLIOS = Map.of(
        "client-123", List.of(new Holding("RNDR", 500), new Holding("KNG", 1000)),
        "client-456", List.of(new Holding("SPY", 150), new Holding("BTC", 5))
    );

    // Inject the RestTemplate and the environment variable we set in Render
    public PortfolioController(RestTemplate restTemplate, 
                               @Value("${MARKET_DATA_SERVICE_URL}") String marketDataServiceUrl) {
        this.restTemplate = restTemplate;
        this.marketDataServiceUrl = marketDataServiceUrl;
    }

    /**
     * This is the main endpoint that Kong will route to.
     * It aggregates data and demonstrates the microservice-to-microservice call.
     */
    @GetMapping("/v1/portfolio/{clientId}")
    public PortfolioResponse getPortfolio(@PathVariable String clientId) {

        // 1. Find the client's portfolio from our mock DB
        List<Holding> basicHoldings = PORTFOLIOS.get(clientId);
        if (basicHoldings == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client portfolio not found");
        }

        // 2. Call the market-data-service for each holding to get live prices
        List<ValuedHolding> valuedHoldings = basicHoldings.stream()
            .map(this::getValuedHolding)
            .collect(Collectors.toList());

        // 3. Aggregate the results
        BigDecimal totalValue = valuedHoldings.stream()
            .map(ValuedHolding::marketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioResponse(clientId, totalValue, valuedHoldings);
    }

    /**
     * Helper method to call the other microservice.
     */
    private ValuedHolding getValuedHolding(Holding holding) {
        // Construct the URL to the internal microservice
        String url = marketDataServiceUrl + "/market/" + holding.symbol();

        try {
            // Make the HTTP call
            MarketPrice price = restTemplate.getForObject(url, MarketPrice.class);

            if (price == null) {
                // Handle case where market service is up but doesn't have the symbol
                return new ValuedHolding(holding.symbol(), holding.quantity(), BigDecimal.ZERO, BigDecimal.ZERO);
            }

            // 4. Perform the aggregation logic
            BigDecimal marketValue = price.price().multiply(BigDecimal.valueOf(holding.quantity()));
            return new ValuedHolding(holding.symbol(), holding.quantity(), price.price(), marketValue);

        } catch (Exception e) {
            // Handle error if the market service is down
            System.err.println("Failed to call market-data-service: " + e.getMessage());
            return new ValuedHolding(holding.symbol(), holding.quantity(), BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

}