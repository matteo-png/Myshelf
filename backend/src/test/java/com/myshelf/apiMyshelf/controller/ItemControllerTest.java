package com.myshelf.apiMyshelf.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.myshelf.apiMyshelf.dto.item.ItemRequest;
import com.myshelf.apiMyshelf.dto.item.ItemResponse;
import com.myshelf.apiMyshelf.model.ItemStatus;
import com.myshelf.apiMyshelf.security.CustomUserDetailsService;
import com.myshelf.apiMyshelf.security.JwtService;
import com.myshelf.apiMyshelf.service.ItemService;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ItemService itemService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;
    @MockitoBean JwtService jwtService;

    @Test
    void getItems_returns_200() throws Exception {
        when(itemService.getItems(2L)).thenReturn(List.of(
                new ItemResponse(
                        1L, 2L, "C",
                        null, null,
                        null, null,
                        "Item", "Desc",
                        BigDecimal.TEN,
                        LocalDate.parse("2025-01-01"),
                        null,
                        ItemStatus.OTHER,
                        List.of("Tag1"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/items").param("collectionId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Item"));
    }

    @Test
    void createItem_returns_201() throws Exception {
        ItemRequest req = new ItemRequest();
        req.setCollectionId(2L);
        req.setName("New item");
        req.setDescription("Desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of(1L));

        when(itemService.createItem(any())).thenReturn(
                new ItemResponse(
                        1L, 2L, "C",
                        null, null,
                        null, null,
                        "New item", "Desc",
                        null, null, null,
                        ItemStatus.OTHER,
                        List.of("Tag1"),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New item"));
    }

    @Test
    void updateItem_returns_200() throws Exception {
        ItemRequest req = new ItemRequest();
        req.setCollectionId(2L);
        req.setName("Updated");
        req.setDescription("Updated desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of());

        when(itemService.updateItem(any(Long.class), any())).thenReturn(
                new ItemResponse(
                        1L, 2L, "C",
                        null, null,
                        null, null,
                        "Updated", "Updated desc",
                        null, null, null,
                        ItemStatus.OTHER,
                        List.of(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteItem_returns_204() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }
}
