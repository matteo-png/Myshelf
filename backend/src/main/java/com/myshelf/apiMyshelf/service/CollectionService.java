package com.myshelf.apiMyshelf.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.collection.CollectionRequest;
import com.myshelf.apiMyshelf.dto.collection.CollectionResponse;
import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    public CollectionService(CollectionRepository collectionRepository,
                             UserRepository userRepository) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
    }

    // Récupérer le user à partir du token JWT
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Collection getUserCollectionOrThrow(Long id) {
        User user = getCurrentUser();
        return collectionRepository.findById(id)
                .filter(c -> c.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    private CollectionResponse toResponse(Collection collection) {
        int itemCount = collection.getItems() != null ? collection.getItems().size() : 0;
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                itemCount,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }

    // GET /api/collections
    public List<CollectionResponse> getMyCollections() {
        User user = getCurrentUser();
        return collectionRepository.findByOwner(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET /api/collections/{id}
    public CollectionResponse getMyCollection(Long id) {
        Collection collection = getUserCollectionOrThrow(id);
        return toResponse(collection);
    }

    // POST /api/collections
    public CollectionResponse createCollection(CollectionRequest request) {
        User user = getCurrentUser();

        Collection collection = Collection.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(user)
                .build();

        Collection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    // PUT /api/collections/{id}
    public CollectionResponse updateCollection(Long id, CollectionRequest request) {
        Collection collection = getUserCollectionOrThrow(id);

        collection.setName(request.getName());
        collection.setDescription(request.getDescription());

        Collection updated = collectionRepository.save(collection);
        return toResponse(updated);
    }

    // DELETE /api/collections/{id}
    public void deleteCollection(Long id) {
        Collection collection = getUserCollectionOrThrow(id);
        collectionRepository.delete(collection);
    }




}
