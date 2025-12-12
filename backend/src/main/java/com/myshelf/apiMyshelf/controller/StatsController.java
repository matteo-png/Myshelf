package com.myshelf.apiMyshelf.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myshelf.apiMyshelf.dto.stats.StatsGroupCountResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsOverviewResponse;
import com.myshelf.apiMyshelf.dto.stats.StatsTimeSeriesPointResponse;
import com.myshelf.apiMyshelf.service.StatsService;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins ="*")
public class StatsController {


    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<StatsOverviewResponse> overview() {
        return ResponseEntity.ok(statsService.overview());
    }

    @GetMapping("/items-by-collection")
    public ResponseEntity<List<StatsGroupCountResponse>> itemsByCollection() {
        return ResponseEntity.ok(statsService.itemsByCollection());
    }

    @GetMapping("/items-by-category")
    public ResponseEntity<List<StatsGroupCountResponse>> itemsByCategory() {
        return ResponseEntity.ok(statsService.itemsByCategory());
    }

    @GetMapping("/items-by-purchase-place")
    public ResponseEntity<List<StatsGroupCountResponse>> itemsByPurchasePlace() {
        return ResponseEntity.ok(statsService.itemsByPurchasePlace());
    }

    @GetMapping("/items-by-status")
    public ResponseEntity<List<StatsGroupCountResponse>> itemsByStatus() {
        return ResponseEntity.ok(statsService.itemsByStatus());
    }

    @GetMapping("/items-by-year")
    public ResponseEntity<List<StatsTimeSeriesPointResponse>> itemsByYear() {
        return ResponseEntity.ok(statsService.itemsByYear());
    }

    @GetMapping("/items-by-month")
    public ResponseEntity<List<StatsTimeSeriesPointResponse>> itemsByMonth(@RequestParam int year) {
        return ResponseEntity.ok(statsService.itemsByMonth(year));
    }
}
