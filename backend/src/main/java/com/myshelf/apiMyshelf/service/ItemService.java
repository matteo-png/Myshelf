package com.myshelf.apiMyshelf.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
import com.myshelf.apiMyshelf.security.CurrentUserService;

@Service
public class ItemService {

private final ItemRepository itemRepository;
    private final CollectionRepository collectionRepository;
    private final CategoryRepository categoryRepository;
    private final PurchasePlaceRepository purchasePlaceRepository;
    private final TagRepository tagRepository;
    private final CurrentUserService currentUserService;
    private final ItemFileStorageService itemFileStorageService;

    public ItemService(ItemRepository itemRepository,
                       CollectionRepository collectionRepository,
                       CategoryRepository categoryRepository,
                       PurchasePlaceRepository purchasePlaceRepository,
                       TagRepository tagRepository,
                       CurrentUserService currentUserService,
                       ItemFileStorageService itemFileStorageService) {
        this.itemRepository = itemRepository;
        this.collectionRepository = collectionRepository;
        this.categoryRepository = categoryRepository;
        this.purchasePlaceRepository = purchasePlaceRepository;
        this.tagRepository = tagRepository;
        this.currentUserService = currentUserService;
        this.itemFileStorageService = itemFileStorageService;
    }


    private Collection getUserCollectionOrThrow(Long collectionId) {
        User user = currentUserService.getCurrentUser();
        return collectionRepository.findById(collectionId)
                .filter(c -> c.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    private Item getUserItemOrThrow(Long itemId) {
        User user = currentUserService.getCurrentUser();
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
                item.getFileName(),
                item.getFileContentType(),
                item.getFichierUrl() != null ? buildFileUrl(item.getId()) : null,
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
        User user = currentUserService.getCurrentUser();

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
        return createItem(request, null);
    }

    public ItemResponse createItem(ItemRequest request, MultipartFile file) {
        User user = currentUserService.getCurrentUser();

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

        attachFileIfPresent(item, file, user);
        Item saved = itemRepository.save(item);
        return toResponse(saved);
    }

    // PUT /api/items/{id}
    public ItemResponse updateItem(Long id, ItemRequest request) {
        return updateItem(id, request, null);
    }

    public ItemResponse updateItem(Long id, ItemRequest request, MultipartFile file) {
        User user = currentUserService.getCurrentUser();
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

        if (Boolean.TRUE.equals(request.getRemoveFile())) {
            removeFile(item, user);
        }
        replaceFileIfPresent(item, file, user);
        Item updated = itemRepository.save(item);
        return toResponse(updated);
    }

    // DELETE /api/items/{id}
    public void deleteItem(Long id) {
        User user = currentUserService.getCurrentUser();
        Item item = getUserItemOrThrow(id);
        itemFileStorageService.deleteIfExists(item.getFichierUrl(), user.getId());
        itemRepository.delete(item);
    }

    public Resource getItemFile(Long id) {
        User user = currentUserService.getCurrentUser();
        Item item = getUserItemOrThrow(id);
        if (item.getFichierUrl() == null || item.getFichierUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        return itemFileStorageService.loadAsResource(item.getFichierUrl(), user.getId());
    }

    private void attachFileIfPresent(Item item, MultipartFile file, User user) {
        ItemFileStorageService.StoredItemFile storedFile = itemFileStorageService.save(user.getId(), file);
        if (storedFile == null) {
            return;
        }
        item.setFichierUrl(storedFile.storedName());
        item.setFileName(storedFile.originalName());
        item.setFileContentType(storedFile.contentType());
    }

    private void replaceFileIfPresent(Item item, MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            return;
        }
        removeFile(item, user);
        attachFileIfPresent(item, file, user);
    }

    private String buildFileUrl(Long itemId) {
        return itemId == null ? null : "/api/items/" + itemId + "/file";
    }

    private void removeFile(Item item, User user) {
        itemFileStorageService.deleteIfExists(item.getFichierUrl(), user.getId());
        item.setFichierUrl(null);
        item.setFileName(null);
        item.setFileContentType(null);
    }
}
