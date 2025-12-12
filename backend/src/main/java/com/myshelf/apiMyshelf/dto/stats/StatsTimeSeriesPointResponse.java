package com.myshelf.apiMyshelf.dto.stats;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatsTimeSeriesPointResponse {

    private final String period;        // "2025" ou "2025-01"
    private final long count;
    private final BigDecimal totalValue;
}
