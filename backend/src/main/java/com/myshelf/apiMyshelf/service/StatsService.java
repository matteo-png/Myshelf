package com.myshelf.apiMyshelf.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.myshelf.apiMyshelf.dto.item.ItemResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsGroupCountResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsOverviewResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsTimeSeriesPointResponse;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CategoryRepository;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.PurchasePlaceRepository;
import com.myshelf.apiMyshelf.repository.TagRepository;
import com.myshelf.apiMyshelf.security.CurrentUserService;

@Service
public class StatsService {

    private final ItemRepository itemRepository;
    private final CollectionRepository collectionRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PurchasePlaceRepository purchasePlaceRepository;
    private final CurrentUserService currentUserService;
    private final ItemService itemService;

    public StatsService(ItemRepository itemRepository,
                        CollectionRepository collectionRepository,
                        CategoryRepository categoryRepository,
                        TagRepository tagRepository,
                        PurchasePlaceRepository purchasePlaceRepository,
                    CurrentUserService currentUserService,
                ItemService itemService) {
        this.itemRepository = itemRepository;
        this.collectionRepository = collectionRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.purchasePlaceRepository = purchasePlaceRepository;
        this.currentUserService = currentUserService;
        this.itemService = itemService;
    }

    public StatsOverviewResponse overview() {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();

        long collections = collectionRepository.countByOwnerEmail(email);
        long items = itemRepository.countByOwnerEmail(email);
        long categories = categoryRepository.countByOwnerEmail(email);
        long tags = tagRepository.countByOwnerEmail(email);
        long places = purchasePlaceRepository.countByOwnerEmail(email);

        BigDecimal totalValue = itemRepository.sumEstimatedValueByOwnerEmail(email);

        return new StatsOverviewResponse(
                collections, items, categories, tags, places, totalValue
        );
    }

    private StatsGroupCountResponse mapRow(Object[] row) {
        String label = (String) row[0];
        long count = (long) row[1];
        BigDecimal totalValue = (BigDecimal) row[2];
        return new StatsGroupCountResponse(label, count, totalValue);
    }

    public List<StatsGroupCountResponse> itemsByCollection() {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();
        return itemRepository.itemsByCollection(email).stream().map(this::mapRow).toList();
    }

    public List<StatsGroupCountResponse> itemsByCategory() {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();
        return itemRepository.itemsByCategory(email).stream().map(this::mapRow).toList();
    }

    public List<StatsGroupCountResponse> itemsByPurchasePlace() {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();
        return itemRepository.itemsByPurchasePlace(email).stream().map(this::mapRow).toList();
    }

    public List<StatsGroupCountResponse> itemsByStatus() {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();
        return itemRepository.itemsByStatus(email).stream().map(this::mapRow).toList();
    }

    public List<StatsTimeSeriesPointResponse> itemsByYear() {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();

    return itemRepository.itemsByYear(email).stream()
            .map(row -> new StatsTimeSeriesPointResponse(
                    String.valueOf((Integer) row[0]),
                    ((Number) row[1]).longValue(),
                    (BigDecimal) row[2]
            ))
            .toList();
    }   

    public List<StatsTimeSeriesPointResponse> itemsByMonth(int year) {
        User user = currentUserService.getCurrentUser();
        String email = user.getEmail();

    // Résultats DB (uniquement les mois présents)
    Map<String, StatsTimeSeriesPointResponse> fromDb = new HashMap<>();
    for (Object[] row : itemRepository.itemsByMonth(email, year)) {
        String period = (String) row[0]; // "YYYY-MM"
        long count = ((Number) row[1]).longValue();
        BigDecimal total = (BigDecimal) row[2];
        fromDb.put(period, new StatsTimeSeriesPointResponse(period, count, total));
    }

    // On renvoie 12 mois fixes (super pratique côté front)
    List<StatsTimeSeriesPointResponse> out = new ArrayList<>();
    for (int m = 1; m <= 12; m++) {
        YearMonth ym = YearMonth.of(year, m);
        String key = String.format("%04d-%02d", ym.getYear(), ym.getMonthValue());

        out.add(fromDb.getOrDefault(
                key,
                new StatsTimeSeriesPointResponse(key, 0, BigDecimal.ZERO)
        ));
    }
    return out;
    }
    public List<ItemResponse> recentItems() {
    return itemService.getRecentItems(5);
    }
}
