package com.myshelf.apiMyshelf.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceRequest;
import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceResponse;
import com.myshelf.apiMyshelf.service.PurchasePlaceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/purchase-places")
@CrossOrigin(origins = "*")
public class PurchasePlaceController {

    private final PurchasePlaceService purchasePlaceService;

    public PurchasePlaceController(PurchasePlaceService purchasePlaceService) {
        this.purchasePlaceService = purchasePlaceService;
    }

    // GET /api/purchase-places
    @GetMapping
    public ResponseEntity<List<PurchasePlaceResponse>> getMyPurchasePlaces() {
        return ResponseEntity.ok(purchasePlaceService.getMyPurchasePlaces());
    }

    // GET /api/purchase-places/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PurchasePlaceResponse> getMyPurchasePlace(@PathVariable Long id) {
        return ResponseEntity.ok(purchasePlaceService.getMyPurchasePlace(id));
    }

    // POST /api/purchase-places
    @PostMapping
    public ResponseEntity<PurchasePlaceResponse> createPurchasePlace(
            @Valid @RequestBody PurchasePlaceRequest request
    ) {
        PurchasePlaceResponse response = purchasePlaceService.createPurchasePlace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/purchase-places/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PurchasePlaceResponse> updatePurchasePlace(
            @PathVariable Long id,
            @Valid @RequestBody PurchasePlaceRequest request
    ) {
        PurchasePlaceResponse response = purchasePlaceService.updatePurchasePlace(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/purchase-places/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchasePlace(@PathVariable Long id) {
        purchasePlaceService.deletePurchasePlace(id);
        return ResponseEntity.noContent().build();
    }
}
