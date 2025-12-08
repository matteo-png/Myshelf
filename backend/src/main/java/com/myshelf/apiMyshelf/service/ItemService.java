package com.myshelf.apiMyshelf.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.item.ItemRequest;
import com.myshelf.apiMyshelf.dto.item.ItemResponse;
import com.myshelf.apiMyshelf.model.Category;
import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.ItemStatus;
import com.myshelf.apiMyshelf.model.PurchasePlace;
import com.myshelf.apiMyshelf.model.Tag;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CategoryRepository;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.PurchasePlaceRepository;
import com.myshelf.apiMyshelf.repository.TagRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Service
public class ItemService {

private final ItemRepository itemRepository;
    private final CollectionRepository collectionRepository;
    private final CategoryRepository categoryRepository;
    private final PurchasePlaceRepository purchasePlaceRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository,
                       CollectionRepository collectionRepository,
                       CategoryRepository categoryRepository,
                       PurchasePlaceRepository purchasePlaceRepository,
                       TagRepository tagRepository,
                       UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.collectionRepository = collectionRepository;
        this.categoryRepository = categoryRepository;
        this.purchasePlaceRepository = purchasePlaceRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    // --- helpers ---

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Collection getUserCollectionOrThrow(Long collectionId) {
        User user = getCurrentUser();
        return collectionRepository.findById(collectionId)
                .filter(c -> c.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    private Item getUserItemOrThrow(Long itemId) {
        User user = getCurrentUser();
        return itemRepository.findById(itemId)
                .filter(i -> i.getCollection().getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    private ItemResponse toResponse(Item item) {
        String collectionName = item.getCollection() != null ? item.getCollection().getName() : null;
        Long collectionId = item.getCollection() != null ? item.getCollection().getId() : null;

        Long categoryId = item.getCategory() != null ? item.getCategory().getId() : null;
        String categoryName = item.getCategory() != null ? item.getCategory().getName() : null;

        Long purchasePlaceId = item.getPurchasePlace() != null ? item.getPurchasePlace().getId() : null;
        String purchasePlaceName = item.getPurchasePlace() != null ? item.getPurchasePlace().getName() : null;

        List<String> tags = item.getTags() != null
                ? item.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : Collections.emptyList();

        return new ItemResponse(
                item.getId(),
                collectionId,
                collectionName,
                categoryId,
                categoryName,
                purchasePlaceId,
                purchasePlaceName,
                item.getName(),
                item.getDescription(),
                item.getEstimatedValue(),
                item.getPurchaseDate(),
                item.getPurchaseUrl(),
                item.getStatus(),
                tags,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private Set<Tag> getTagsForUser(List<Long> tagIds, User user) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        // on filtre pour ne garder que les tags de ce user
        return tags.stream()
                .filter(t -> t.getOwner() != null && t.getOwner().getId().equals(user.getId()))
                .collect(Collectors.toSet());
    }

    // --- CRUD ---

    // GET /api/items?collectionId=...
    public List<ItemResponse> getItems(Long collectionId) {
        User user = getCurrentUser();

        List<Item> items;
        if (collectionId != null) {
            Collection collection = getUserCollectionOrThrow(collectionId);
            items = itemRepository.findByCollection(collection);
        } else {
            // si tu veux lister tous les items du user
            items = itemRepository.findAll().stream()
                    .filter(i -> i.getCollection().getOwner().getId().equals(user.getId()))
                    .toList();
        }

        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    // GET /api/items/{id}
    public ItemResponse getItem(Long id) {
        Item item = getUserItemOrThrow(id);
        return toResponse(item);
    }

    // POST /api/items
    public ItemResponse createItem(ItemRequest request) {
        User user = getCurrentUser();

        Collection collection = getUserCollectionOrThrow(request.getCollectionId());

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        }

        PurchasePlace purchasePlace = null;
        if (request.getPurchasePlaceId() != null) {
            purchasePlace = purchasePlaceRepository.findById(request.getPurchasePlaceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase place not found"));
        }

        Set<Tag> tags = getTagsForUser(request.getTagIds(), user);

        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .collection(collection)
                .category(category)
                .purchasePlace(purchasePlace)
                .estimatedValue(request.getEstimatedValue())
                .purchaseDate(request.getPurchaseDate())
                .purchaseUrl(request.getPurchaseUrl())
                .status(request.getStatus() != null ? request.getStatus() : ItemStatus.OTHER)
                .tags(tags)
                .build();

        Item saved = itemRepository.save(item);
        return toResponse(saved);
    }

    // PUT /api/items/{id}
    public ItemResponse updateItem(Long id, ItemRequest request) {
        User user = getCurrentUser();
        Item item = getUserItemOrThrow(id);

        
        if (request.getCollectionId() != null &&
                !request.getCollectionId().equals(item.getCollection().getId())) {
            Collection newCollection = getUserCollectionOrThrow(request.getCollectionId());
            item.setCollection(newCollection);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            item.setCategory(category);
        } else {
            item.setCategory(null);
        }

        if (request.getPurchasePlaceId() != null) {
            PurchasePlace purchasePlace = purchasePlaceRepository.findById(request.getPurchasePlaceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase place not found"));
            item.setPurchasePlace(purchasePlace);
        } else {
            item.setPurchasePlace(null);
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setEstimatedValue(request.getEstimatedValue());
        item.setPurchaseDate(request.getPurchaseDate());
        item.setPurchaseUrl(request.getPurchaseUrl());
        item.setStatus(request.getStatus() != null ? request.getStatus() : item.getStatus());

        Set<Tag> tags = getTagsForUser(request.getTagIds(), user);
        item.setTags(tags);

        Item updated = itemRepository.save(item);
        return toResponse(updated);
    }

    // DELETE /api/items/{id}
    public void deleteItem(Long id) {
        Item item = getUserItemOrThrow(id);
        itemRepository.delete(item);
    }
}
