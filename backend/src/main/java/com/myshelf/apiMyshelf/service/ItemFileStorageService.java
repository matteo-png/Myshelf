package com.myshelf.apiMyshelf.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ItemFileStorageService {

    private final Path rootDirectory;

    public ItemFileStorageService(@Value("${app.item-files.root:uploads/item-files}") String rootDirectory) {
        this.rootDirectory = Paths.get(rootDirectory).toAbsolutePath().normalize();
    }

    public StoredItemFile save(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();
        String safeOriginalName = sanitizeFilename(originalName == null ? "file" : originalName);
        String storedName = UUID.randomUUID() + "-" + safeOriginalName;
        Path userDirectory = rootDirectory.resolve(String.valueOf(userId)).normalize();
        Path target = userDirectory.resolve(storedName).normalize();

        if (!target.startsWith(userDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try {
            Files.createDirectories(userDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store file", ex);
        }

        return new StoredItemFile(
                storedName,
                safeOriginalName,
                file.getContentType(),
                target
        );
    }

    public Resource loadAsResource(String storedName, Long userId) {
        Path path = resolvePath(storedName, userId);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read file", ex);
        }
    }

    public void deleteIfExists(String storedName, Long userId) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        Path path = resolvePath(storedName, userId);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete file", ex);
        }
    }

    private Path resolvePath(String storedName, Long userId) {
        Path userDirectory = rootDirectory.resolve(String.valueOf(userId)).normalize();
        Path resolved = userDirectory.resolve(storedName).normalize();
        if (!resolved.startsWith(userDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        return resolved;
    }

    private String sanitizeFilename(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        String sanitized = normalized.replace("\\", "_").replace("/", "_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "file" : sanitized;
    }

    public record StoredItemFile(
            String storedName,
            String originalName,
            String contentType,
            Path path
    ) {}
}
