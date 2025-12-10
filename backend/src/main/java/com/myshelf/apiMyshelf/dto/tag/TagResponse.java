package com.myshelf.apiMyshelf.dto.tag;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagResponse {


    private final Long id;
    private final String name;
    private final int itemCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
