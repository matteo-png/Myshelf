package com.myshelf.apiMyshelf.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;


class CollectionServiceTest {

    @Mock CollectionRepository collectionRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CollectionService collectionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContextHolder -> email
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@myshelf.local");

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyCollections_returns_only_user_collections() {
        User user = User.builder().email("test@myshelf.local").build();
        user.setId(1L);

        when(userRepository.findByEmail("test@myshelf.local")).thenReturn(Optional.of(user));
        when(collectionRepository.findByOwner(user)).thenReturn(List.of(
                Collection.builder().name("A").owner(user).build(),
                Collection.builder().name("B").owner(user).build()
        ));

        var result = collectionService.getMyCollections();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("A");
        verify(collectionRepository).findByOwner(user);
    }
}
