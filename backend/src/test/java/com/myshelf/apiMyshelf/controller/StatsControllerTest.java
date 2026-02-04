package com.myshelf.apiMyshelf.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myshelf.apiMyshelf.dto.stats.StatsOverviewResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsTimeSeriesPointResponse;
import com.myshelf.apiMyshelf.security.CustomUserDetailsService;
import com.myshelf.apiMyshelf.security.JwtService;
import com.myshelf.apiMyshelf.service.StatsService;

@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean StatsService statsService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void overview_returns_200() throws Exception {
        when(statsService.overview()).thenReturn(
                new StatsOverviewResponse(2, 10, 3, 4, 1, new BigDecimal("123.45"))
        );

        mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemsCount").value(10))
                .andExpect(jsonPath("$.totalEstimatedValue").value(123.45));
    }

    @Test
    void itemsByMonth_returns_200() throws Exception {
        when(statsService.itemsByMonth(2025)).thenReturn(List.of(
                new StatsTimeSeriesPointResponse("2025-01", 2, new BigDecimal("10.00"))
        ));

        mockMvc.perform(get("/api/stats/items-by-month").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2025-01"));
    }

    @Test
    void itemsByYear_returns_200() throws Exception {
        when(statsService.itemsByYear()).thenReturn(List.of(
                new StatsTimeSeriesPointResponse("2025", 2, new BigDecimal("10.00"))
        ));

        mockMvc.perform(get("/api/stats/items-by-year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2025"));
    }
}
