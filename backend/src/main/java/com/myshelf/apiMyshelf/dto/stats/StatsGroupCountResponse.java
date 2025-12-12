package com.myshelf.apiMyshelf.dto.stats;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatsGroupCountResponse {

    private final String label;
    private final long count;
    private final BigDecimal totalValue;
}
