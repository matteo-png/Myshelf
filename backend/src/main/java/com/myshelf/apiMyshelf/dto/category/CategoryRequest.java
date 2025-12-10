package com.myshelf.apiMyshelf.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {


    @NotBlank
    @Size(max = 255)
    private String name;
}
