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

import com.myshelf.apiMyshelf.dto.tag.TagRequest;
import com.myshelf.apiMyshelf.dto.tag.TagResponse;
import com.myshelf.apiMyshelf.service.TagService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // GET /api/tags
    @GetMapping
    public ResponseEntity<List<TagResponse>> getMyTags() {
        return ResponseEntity.ok(tagService.getMyTags());
    }

    // GET /api/tags/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getMyTag(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getMyTag(id));
    }

    // POST /api/tags
    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody TagRequest request
    ) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/tags/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request
    ) {
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/tags/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
