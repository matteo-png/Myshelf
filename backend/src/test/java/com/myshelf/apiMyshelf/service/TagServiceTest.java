package com.myshelf.apiMyshelf.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.dto.tag.TagRequest;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.Tag;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.TagRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

class TagServiceTest {

    @Mock TagRepository tagRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TagService tagService;

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
    void createTag_ok() {
        TagRequest req = new TagRequest();
        req.setName("Sale");

        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        var res = tagService.createTag(req);

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getName()).isEqualTo("Sale");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void updateTag_not_owner_should_throw_404() {
        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Tag tag = Tag.builder().name("Old").owner(other).build();
        tag.setId(5L);

        when(tagRepository.findById(5L)).thenReturn(Optional.of(tag));

        TagRequest req = new TagRequest();
        req.setName("New");

        assertThatThrownBy(() -> tagService.updateTag(5L, req))
                .isInstanceOf(ResponseStatusException.class);

        verify(tagRepository, never()).save(any());
    }

    @Test
    void deleteTag_detaches_from_items_then_deletes() {
        Tag tag = Tag.builder().name("T").owner(user).build();
        tag.setId(7L);

        // item avec ce tag
        Item item = Item.builder().name("I").build();
        Set<Tag> itemTags = new HashSet<>();
        itemTags.add(tag);
        item.setTags(itemTags);

        // côté tag -> items
        Set<Item> tagItems = new HashSet<>();
        tagItems.add(item);
        tag.setItems(tagItems);

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));

        tagService.deleteTag(7L);

        // le tag doit être retiré de item.tags
        assertThat(item.getTags()).doesNotContain(tag);

        verify(tagRepository).delete(tag);
    }
}
