package com.myshelf.apiMyshelf.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.category.CategoryRequest;
import com.myshelf.apiMyshelf.model.Category;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CategoryRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;


class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock UserRepository userRepository;
    @Mock ItemRepository itemRepository;

    @InjectMocks CategoryService categoryService;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@myshelf.local");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        user = User.builder()
                .email("test@myshelf.local")
                .passwordHash("x")
                .displayName("Test")
                .build();
        user.setId(1L);

        when(userRepository.findByEmail("test@myshelf.local")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCategory_ok() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Books");

        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        var res = categoryService.createCategory(req);

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getName()).isEqualTo("Books");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_not_owner_should_throw_404() {
        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Category cat = Category.builder().name("Old").owner(other).build();
        cat.setId(5L);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(cat));

        CategoryRequest req = new CategoryRequest();
        req.setName("New");

        assertThatThrownBy(() -> categoryService.updateCategory(5L, req))
                .isInstanceOf(ResponseStatusException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_used_by_items_should_throw_400() {
        Category cat = Category.builder().name("Used").owner(user).build();
        cat.setId(7L);

        when(categoryRepository.findById(7L)).thenReturn(Optional.of(cat));
        when(itemRepository.findByCategory(cat)).thenReturn(List.of(
                Item.builder().name("I1").build()
        ));

        ResponseStatusException ex = catchThrowableOfType(
                () -> categoryService.deleteCategory(7L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_not_used_should_delete() {
        Category cat = Category.builder().name("Free").owner(user).build();
        cat.setId(8L);

        when(categoryRepository.findById(8L)).thenReturn(Optional.of(cat));
        when(itemRepository.findByCategory(cat)).thenReturn(List.of());

        categoryService.deleteCategory(8L);

        verify(categoryRepository).delete(cat);
    }
}
