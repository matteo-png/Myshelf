package com.myshelf.apiMyshelf.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceRequest;
import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceResponse;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.PurchasePlace;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.PurchasePlaceRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Service
public class PurchasePlaceService {


    private final PurchasePlaceRepository purchasePlaceRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public PurchasePlaceService(PurchasePlaceRepository purchasePlaceRepository,
                                UserRepository userRepository,
                                ItemRepository itemRepository) {
        this.purchasePlaceRepository = purchasePlaceRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private PurchasePlace getUserPurchasePlaceOrThrow(Long id) {
        User user = getCurrentUser();
        return purchasePlaceRepository.findById(id)
                .filter(p -> p.getOwner() != null && p.getOwner().getId().equals(user.getId()))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase place not found"));
    }

    private PurchasePlaceResponse toResponse(PurchasePlace place) {
        int itemCount = place.getItems() != null ? place.getItems().size() : 0;

        return new PurchasePlaceResponse(
                place.getId(),
                place.getName(),
                place.getType(),
                place.getWebsiteUrl(),
                itemCount,
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }

    // GET /api/purchase-places
    public List<PurchasePlaceResponse> getMyPurchasePlaces() {
        User user = getCurrentUser();
        return purchasePlaceRepository.findByOwner(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET /api/purchase-places/{id}
    public PurchasePlaceResponse getMyPurchasePlace(Long id) {
        PurchasePlace place = getUserPurchasePlaceOrThrow(id);
        return toResponse(place);
    }

    // POST /api/purchase-places
    public PurchasePlaceResponse createPurchasePlace(PurchasePlaceRequest request) {
        User user = getCurrentUser();

        PurchasePlace place = PurchasePlace.builder()
                .name(request.getName())
                .type(request.getType())
                .websiteUrl(request.getWebsiteUrl())
                .owner(user)
                .build();

        PurchasePlace saved = purchasePlaceRepository.save(place);
        return toResponse(saved);
    }

    // PUT /api/purchase-places/{id}
    public PurchasePlaceResponse updatePurchasePlace(Long id, PurchasePlaceRequest request) {
        PurchasePlace place = getUserPurchasePlaceOrThrow(id);

        place.setName(request.getName());
        place.setType(request.getType());
        place.setWebsiteUrl(request.getWebsiteUrl());

        PurchasePlace updated = purchasePlaceRepository.save(place);
        return toResponse(updated);
    }

    // DELETE /api/purchase-places/{id}
    public void deletePurchasePlace(Long id) {
        PurchasePlace place = getUserPurchasePlaceOrThrow(id);

        List<Item> items = itemRepository.findByPurchasePlace(place);
        if (!items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot delete purchase place: it is used by some items"
            );
        }

        purchasePlaceRepository.delete(place);
    }
}
