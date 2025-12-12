package com.myshelf.apiMyshelf.dto.stats;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatsOverviewResponse {

    private final long collectionsCount;
    private final long itemsCount;
    private final long categoriesCount;
    private final long tagsCount;
    private final long purchasePlacesCount;
    private final BigDecimal totalEstimatedValue;
}
