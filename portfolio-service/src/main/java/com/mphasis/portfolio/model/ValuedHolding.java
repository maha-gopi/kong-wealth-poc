package com.mphasis.portfolio.model;

import java.math.BigDecimal;

// DTO for one holding in our final response
public record ValuedHolding(String symbol, int quantity, BigDecimal currentPrice, BigDecimal marketValue) {}

