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
import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceRequest;
import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceResponse;
import com.myshelf.apiMyshelf.model.PurchasePlaceType;
import com.myshelf.apiMyshelf.security.CustomUserDetailsService;
import com.myshelf.apiMyshelf.security.JwtService;
import com.myshelf.apiMyshelf.service.PurchasePlaceService;

@WebMvcTest(PurchasePlaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class PurchasePlaceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean PurchasePlaceService purchasePlaceService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void getPurchasePlaces_returns_200() throws Exception {
        when(purchasePlaceService.getMyPurchasePlaces()).thenReturn(List.of(
                new PurchasePlaceResponse(
                        1L, "Amazon", PurchasePlaceType.ONLINE, "https://amazon.fr",
                        0, LocalDateTime.now(), LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/purchase-places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Amazon"));
    }

    @Test
    void createPurchasePlace_returns_201() throws Exception {
        PurchasePlaceRequest req = new PurchasePlaceRequest();
        req.setName("Fnac");
        req.setType(PurchasePlaceType.MAGASIN);
        req.setWebsiteUrl("https://fnac.com");

        when(purchasePlaceService.createPurchasePlace(any())).thenReturn(
                new PurchasePlaceResponse(
                        1L, "Fnac", PurchasePlaceType.MAGASIN, "https://fnac.com",
                        0, LocalDateTime.now(), LocalDateTime.now()
                )
        );

        mockMvc.perform(post("/api/purchase-places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fnac"));
    }

    @Test
    void updatePurchasePlace_returns_200() throws Exception {
        PurchasePlaceRequest req = new PurchasePlaceRequest();
        req.setName("Amazon FR");
        req.setType(PurchasePlaceType.ONLINE);
        req.setWebsiteUrl("https://amazon.fr");

        when(purchasePlaceService.updatePurchasePlace(any(Long.class), any())).thenReturn(
                new PurchasePlaceResponse(
                        1L, "Amazon FR", PurchasePlaceType.ONLINE, "https://amazon.fr",
                        0, LocalDateTime.now(), LocalDateTime.now()
                )
        );

        mockMvc.perform(put("/api/purchase-places/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amazon FR"));
    }

    @Test
    void deletePurchasePlace_returns_204() throws Exception {
        mockMvc.perform(delete("/api/purchase-places/1"))
                .andExpect(status().isNoContent());
    }

}
