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

import com.myshelf.apiMyshelf.dto.collection.CollectionRequest;
import com.myshelf.apiMyshelf.dto.collection.CollectionResponse;
import com.myshelf.apiMyshelf.service.CollectionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins="*")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    // GET /api/collections
    @GetMapping
    public ResponseEntity<List<CollectionResponse>> getMyCollections() {
        return ResponseEntity.ok(collectionService.getMyCollections());
    }

    // GET /api/collections/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> getMyCollection(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.getMyCollection(id));
    }

    // POST /api/collections
    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(
            @Valid @RequestBody CollectionRequest request
    ) {
        CollectionResponse response = collectionService.createCollection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/collections/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponse> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody CollectionRequest request
    ) {
        CollectionResponse response = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/collections/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }
}
