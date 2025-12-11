package com.myshelf.apiMyshelf.dto.purchaseplace;

import com.myshelf.apiMyshelf.model.PurchasePlaceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchasePlaceRequest {


    @NotBlank
    @Size(max = 255)
    private String name;

    private PurchasePlaceType type;

    @Size(max = 500)
    private String websiteUrl;
}
