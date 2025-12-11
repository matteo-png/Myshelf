package com.myshelf.apiMyshelf.dto.purchaseplace;

import java.time.LocalDateTime;

import com.myshelf.apiMyshelf.model.PurchasePlaceType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchasePlaceResponse {

    private final Long id;
    private final String name;
    private final PurchasePlaceType type;
    private final String websiteUrl;
    private final int itemCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
