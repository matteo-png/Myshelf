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
import com.myshelf.apiMyshelf.dto.tag.TagRequest;
import com.myshelf.apiMyshelf.dto.tag.TagResponse;
import com.myshelf.apiMyshelf.security.CustomUserDetailsService;
import com.myshelf.apiMyshelf.security.JwtService;
import com.myshelf.apiMyshelf.service.TagService;


@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TagService tagService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void getTags_returns_200() throws Exception {
        when(tagService.getMyTags()).thenReturn(List.of(
                new TagResponse(1L, "Sale", 0, LocalDateTime.now(), LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sale"));
    }

    @Test
    void createTag_returns_201() throws Exception {
        TagRequest req = new TagRequest();
        req.setName("New");

        when(tagService.createTag(any())).thenReturn(
                new TagResponse(1L, "New", 0, LocalDateTime.now(), LocalDateTime.now())
        );

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void updateTag_returns_200() throws Exception {
        TagRequest req = new TagRequest();
        req.setName("Updated");

        when(tagService.updateTag(any(Long.class), any())).thenReturn(
                new TagResponse(1L, "Updated", 0, LocalDateTime.now(), LocalDateTime.now())
        );

        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteTag_returns_204() throws Exception {
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());
    }

}
