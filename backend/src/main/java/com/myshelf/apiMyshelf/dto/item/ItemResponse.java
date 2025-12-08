package com.myshelf.apiMyshelf.dto.item;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.myshelf.apiMyshelf.model.ItemStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemResponse {

    private final Long id;
    private final Long collectionId;
    private final String collectionName;

    private final Long categoryId;
    private final String categoryName;

    private final Long purchasePlaceId;
    private final String purchasePlaceName;

    private final String name;
    private final String description;
    private final BigDecimal estimatedValue;
    private final LocalDate purchaseDate;
    private final String purchaseUrl;
    private final ItemStatus status;

    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
