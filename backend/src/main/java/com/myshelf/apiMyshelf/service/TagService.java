package com.myshelf.apiMyshelf.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.tag.TagRequest;
import com.myshelf.apiMyshelf.dto.tag.TagResponse;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.Tag;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.TagRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public TagService(TagRepository tagRepository,
                      UserRepository userRepository) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Tag getUserTagOrThrow(Long id) {
        User user = getCurrentUser();
        return tagRepository.findById(id)
                .filter(t -> t.getOwner() != null && t.getOwner().getId().equals(user.getId()))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
    }

    private TagResponse toResponse(Tag tag) {
        int itemCount = tag.getItems() != null ? tag.getItems().size() : 0;

        return new TagResponse(
                tag.getId(),
                tag.getName(),
                itemCount,
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }

    // GET /api/tags
    public List<TagResponse> getMyTags() {
        User user = getCurrentUser();
        return tagRepository.findByOwner(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET /api/tags/{id}
    public TagResponse getMyTag(Long id) {
        Tag tag = getUserTagOrThrow(id);
        return toResponse(tag);
    }

    // POST /api/tags
    public TagResponse createTag(TagRequest request) {
        User user = getCurrentUser();

        Tag tag = Tag.builder()
                .name(request.getName())
                .owner(user)
                .build();

        Tag saved = tagRepository.save(tag);
        return toResponse(saved);
    }

    // PUT /api/tags/{id}
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = getUserTagOrThrow(id);

        tag.setName(request.getName());

        Tag updated = tagRepository.save(tag);
        return toResponse(updated);
    }

    // DELETE /api/tags/{id}
    public void deleteTag(Long id) {
        Tag tag = getUserTagOrThrow(id);

        // Détacher le tag de tous les items avant suppression
        if (tag.getItems() != null) {
            for (Item item : tag.getItems()) {
                item.getTags().remove(tag);
            }
        }

        tagRepository.delete(tag);
    }
}
