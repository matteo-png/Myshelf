package com.myshelf.apiMyshelf.dto.item;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.myshelf.apiMyshelf.model.ItemStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequest {


    @NotNull
    private Long collectionId;

    private Long categoryId;
    private Long purchasePlaceId;


    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private BigDecimal estimatedValue;

    private LocalDate purchaseDate;

    @Size(max = 1000)
    private String purchaseUrl;

    private ItemStatus status;

    private List<Long> tagIds;  
}
