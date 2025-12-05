package com.myshelf.apiMyshelf.dto.collection;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final int itemCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

}
