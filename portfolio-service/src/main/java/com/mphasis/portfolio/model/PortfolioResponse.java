package com.mphasis.portfolio.model;

import java.math.BigDecimal;
import java.util.List;

// DTO for the final aggregated response
public record PortfolioResponse(String clientId, BigDecimal totalPortfolioValue, List<ValuedHolding> holdings) {}

