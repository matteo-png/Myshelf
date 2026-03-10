package com.myshelf.apiMyshelf.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.myshelf.apiMyshelf.dto.item.ItemRequest;
import com.myshelf.apiMyshelf.dto.item.ItemResponse;
import com.myshelf.apiMyshelf.service.ItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {
    

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // GET /api/items?collectionId=...
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getItems(
            @RequestParam(required = false) Long collectionId
    ) {
        return ResponseEntity.ok(itemService.getItems(collectionId));
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getItemFile(@PathVariable Long id) {
        ItemResponse item = itemService.getItem(id);
        Resource resource = itemService.getItemFile(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (item.getFileContentType() != null && !item.getFileContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(item.getFileContentType());
        }

        ResponseEntity.BodyBuilder response = ResponseEntity.ok().contentType(mediaType);
        if (item.getFileName() != null && !item.getFileName().isBlank()) {
            response.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + item.getFileName() + "\"");
        }
        return response.body(resource);
    }

    // POST /api/items
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> createItemMultipart(
            @Valid @RequestPart("item") ItemRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        ItemResponse response = itemService.createItem(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse response = itemService.updateItem(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> updateItemMultipart(
            @PathVariable Long id,
            @Valid @RequestPart("item") ItemRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        ItemResponse response = itemService.updateItem(id, request, file);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
