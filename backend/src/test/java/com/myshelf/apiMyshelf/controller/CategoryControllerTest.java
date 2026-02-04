package com.myshelf.apiMyshelf.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshelf.apiMyshelf.dto.category.CategoryRequest;
import com.myshelf.apiMyshelf.dto.category.CategoryResponse;
import com.myshelf.apiMyshelf.security.CustomUserDetailsService;
import com.myshelf.apiMyshelf.security.JwtService;
import com.myshelf.apiMyshelf.service.CategoryService;


@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CategoryService categoryService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void getCategories_returns_200() throws Exception {
        when(categoryService.getMyCategories()).thenReturn(List.of(
                new CategoryResponse(1L, "Books", LocalDateTime.now(), LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Books"));
    }

    @Test
    void createCategory_returns_201() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("New");

        when(categoryService.createCategory(any())).thenReturn(
                new CategoryResponse(1L, "New", LocalDateTime.now(), LocalDateTime.now())
        );

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void updateCategory_returns_200() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Updated");

        when(categoryService.updateCategory(any(Long.class), any())).thenReturn(
                new CategoryResponse(1L, "Updated", LocalDateTime.now(), LocalDateTime.now())
        );

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteCategory_returns_204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }
}
