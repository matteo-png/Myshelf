package com.myshelf.apiMyshelf.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.category.CategoryRequest;
import com.myshelf.apiMyshelf.dto.category.CategoryResponse;
import com.myshelf.apiMyshelf.model.Category;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CategoryRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           ItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
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

    private Category getUserCategoryOrThrow(Long id) {
        User user = getCurrentUser();
        return categoryRepository.findById(id)
                .filter(c -> c.getOwner() != null && c.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    // GET /api/categories
    public List<CategoryResponse> getMyCategories() {
        User user = getCurrentUser();
        return categoryRepository.findByOwner(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET /api/categories/{id}
    public CategoryResponse getMyCategory(Long id) {
        Category category = getUserCategoryOrThrow(id);
        return toResponse(category);
    }

    // POST /api/categories
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        Category category = Category.builder()
                .name(request.getName())
                .owner(user)
                .build();

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    // PUT /api/categories/{id}
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getUserCategoryOrThrow(id);
        category.setName(request.getName());

        Category updated = categoryRepository.save(category);
        return toResponse(updated);
    }

    // DELETE /api/categories/{id}
    public void deleteCategory(Long id) {
        Category category = getUserCategoryOrThrow(id);

        // Vérifier si des items utilisent cette catégorie
        List<Item> itemsWithCategory = itemRepository.findByCategory(category);
        if (!itemsWithCategory.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot delete category: it is used by some items"
            );
        }

        categoryRepository.delete(category);
    }
}
