package com.myshelf.apiMyshelf.service;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.myshelf.apiMyshelf.dto.stats.StatsOverviewResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsTimeSeriesPointResponse;
import com.myshelf.apiMyshelf.repository.CategoryRepository;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.PurchasePlaceRepository;
import com.myshelf.apiMyshelf.repository.TagRepository;

class StatsServiceTest {

    @Mock ItemRepository itemRepository;
    @Mock CollectionRepository collectionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TagRepository tagRepository;
    @Mock PurchasePlaceRepository purchasePlaceRepository;

    @InjectMocks StatsService statsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@myshelf.local");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void overview_returns_expected_counts_and_totalValue() {
        when(collectionRepository.countByOwnerEmail("test@myshelf.local")).thenReturn(2L);
        when(itemRepository.countByOwnerEmail("test@myshelf.local")).thenReturn(10L);
        when(categoryRepository.countByOwnerEmail("test@myshelf.local")).thenReturn(3L);
        when(tagRepository.countByOwnerEmail("test@myshelf.local")).thenReturn(4L);
        when(purchasePlaceRepository.countByOwnerEmail("test@myshelf.local")).thenReturn(1L);
        when(itemRepository.sumEstimatedValueByOwnerEmail("test@myshelf.local")).thenReturn(new BigDecimal("123.45"));

        StatsOverviewResponse res = statsService.overview();

        assertThat(res.getCollectionsCount()).isEqualTo(2);
        assertThat(res.getItemsCount()).isEqualTo(10);
        assertThat(res.getCategoriesCount()).isEqualTo(3);
        assertThat(res.getTagsCount()).isEqualTo(4);
        assertThat(res.getPurchasePlacesCount()).isEqualTo(1);
        assertThat(res.getTotalEstimatedValue()).isEqualByComparingTo("123.45");
    }

    @Test
    void itemsByMonth_returns_12_months_with_zero_for_missing() {
        // DB renvoie seulement Janvier + Mars
        when(itemRepository.itemsByMonth("test@myshelf.local", 2025)).thenReturn(List.of(
                new Object[]{"2025-01", 2L, new BigDecimal("10.00")},
                new Object[]{"2025-03", 1L, new BigDecimal("5.00")}
        ));

        List<StatsTimeSeriesPointResponse> res = statsService.itemsByMonth(2025);

        assertThat(res).hasSize(12);

        // Janvier
        assertThat(res.get(0).getPeriod()).isEqualTo("2025-01");
        assertThat(res.get(0).getCount()).isEqualTo(2);
        assertThat(res.get(0).getTotalValue()).isEqualByComparingTo("10.00");

        // Février (0)
        assertThat(res.get(1).getPeriod()).isEqualTo("2025-02");
        assertThat(res.get(1).getCount()).isEqualTo(0);
        assertThat(res.get(1).getTotalValue()).isEqualByComparingTo("0");

        // Mars
        assertThat(res.get(2).getPeriod()).isEqualTo("2025-03");
        assertThat(res.get(2).getCount()).isEqualTo(1);
        assertThat(res.get(2).getTotalValue()).isEqualByComparingTo("5.00");
    }

    @Test
    void itemsByYear_maps_rows_correctly() {
        when(itemRepository.itemsByYear("test@myshelf.local")).thenReturn(List.of(
                new Object[]{2024, 3L, new BigDecimal("50.00")},
                new Object[]{2025, 2L, new BigDecimal("10.00")}
        ));

        var res = statsService.itemsByYear();

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getPeriod()).isEqualTo("2024");
        assertThat(res.get(0).getCount()).isEqualTo(3);
        assertThat(res.get(0).getTotalValue()).isEqualByComparingTo("50.00");
    }
}
