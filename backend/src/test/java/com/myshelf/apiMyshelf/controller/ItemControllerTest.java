package com.myshelf.apiMyshelf.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
        when(itemService.getItems(2L)).thenReturn(List.of(itemResponse("Item", "Desc")));

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

        when(itemService.createItem(any())).thenReturn(itemResponse("New item", "Desc"));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New item"));
    }

    @Test
    void createItemMultipart_returns_201() throws Exception {
        ItemRequest req = new ItemRequest();
        req.setCollectionId(2L);
        req.setName("New item");
        req.setDescription("Desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of(1L));

        MockMultipartFile itemPart = new MockMultipartFile(
                "item",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        when(itemService.createItem(any(), any())).thenReturn(new ItemResponse(
                1L, 2L, "C",
                null, null,
                null, null,
                "New item", "Desc",
                null, null, null,
                ItemStatus.OTHER,
                "invoice.pdf", "application/pdf", "/api/items/1/file",
                List.of("Tag1"),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        mockMvc.perform(multipart("/api/items")
                        .file(itemPart)
                        .file(filePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileUrl").value("/api/items/1/file"))
                .andExpect(jsonPath("$.fileName").value("invoice.pdf"));
    }

    @Test
    void updateItem_returns_200() throws Exception {
        ItemRequest req = new ItemRequest();
        req.setCollectionId(2L);
        req.setName("Updated");
        req.setDescription("Updated desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of());

        when(itemService.updateItem(anyLong(), any())).thenReturn(itemResponse("Updated", "Updated desc"));

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void getItemFile_returns_binary_payload() throws Exception {
        when(itemService.getItem(1L)).thenReturn(new ItemResponse(
                1L, 2L, "C",
                null, null,
                null, null,
                "Item", "Desc",
                BigDecimal.TEN,
                LocalDate.parse("2025-01-01"),
                null,
                ItemStatus.OTHER,
                "invoice.pdf", "application/pdf", "/api/items/1/file",
                List.of("Tag1"),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        when(itemService.getItemFile(1L)).thenReturn(new ByteArrayResource("pdf-content".getBytes()));

        mockMvc.perform(get("/api/items/1/file"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"invoice.pdf\""))
                .andExpect(content().bytes("pdf-content".getBytes()));
    }

    @Test
    void deleteItem_returns_204() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

    private ItemResponse itemResponse(String name, String description) {
        return new ItemResponse(
                1L, 2L, "C",
                null, null,
                null, null,
                name, description,
                BigDecimal.TEN,
                LocalDate.parse("2025-01-01"),
                null,
                ItemStatus.OTHER,
                null, null, null,
                List.of("Tag1"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
