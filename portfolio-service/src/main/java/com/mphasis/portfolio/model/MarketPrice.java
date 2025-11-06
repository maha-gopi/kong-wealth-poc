package com.mphasis.portfolio.model;

import java.math.BigDecimal;

// DTO for the response from the *other* microservice
public record MarketPrice(String symbol, BigDecimal price) {}