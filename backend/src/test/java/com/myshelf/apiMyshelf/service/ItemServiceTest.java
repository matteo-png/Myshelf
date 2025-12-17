package com.myshelf.apiMyshelf.service;

import java.util.List;
import java.util.Optional;

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

import com.myshelf.apiMyshelf.dto.item.ItemRequest;
import com.myshelf.apiMyshelf.dto.item.ItemResponse;
import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.ItemStatus;
import com.myshelf.apiMyshelf.model.Tag;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CategoryRepository;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.PurchasePlaceRepository;
import com.myshelf.apiMyshelf.repository.TagRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;


class ItemServiceTest {

    @Mock ItemRepository itemRepository;
    @Mock CollectionRepository collectionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock PurchasePlaceRepository purchasePlaceRepository;
    @Mock TagRepository tagRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ItemService itemService;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock auth email
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
    void createItem_ok() {
        Collection collection = Collection.builder().name("C").owner(user).build();
        collection.setId(10L);

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("Item 1");
        req.setDescription("Desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of()); // no tags

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(collection));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        var res = itemService.createItem(req);

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getName()).isEqualTo("Item 1");
        assertThat(res.getCollectionId()).isEqualTo(10L);

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_collection_not_owned_should_throw_404() {
        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Collection collection = Collection.builder().name("C").owner(other).build();
        collection.setId(10L);

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("Item X");

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(collection));

        assertThatThrownBy(() -> itemService.createItem(req))
                .isInstanceOf(ResponseStatusException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_replaces_tags_only_with_user_owned() {
        // Article existant appartenant à l'utilisateur
        Collection collection = Collection.builder().name("C").owner(user).build();
        collection.setId(10L);

        Item item = Item.builder()
                .name("Old")
                .collection(collection)
                .status(ItemStatus.OTHER)
                .build();
        item.setId(100L);

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        // tags from DB: one owned by user, one owned by other -> doivent être filtrées par le service
        Tag myTag = Tag.builder().name("Mine").owner(user).build();
        myTag.setId(1L);

        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Tag otherTag = Tag.builder().name("Other").owner(other).build();
        otherTag.setId(2L);

        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(myTag, otherTag));

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("New");
        req.setDescription("New desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of(1L, 2L));

        
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = itemService.updateItem(100L, req);

        //  ItemResponse renvoie tags sous names (List<String>)
        assertThat(res.getName()).isEqualTo("New");
        assertThat(res.getTags()).contains("Mine");
        assertThat(res.getTags()).doesNotContain("Other");
    }


    @Test
    void updateItem_change_collection_not_owned_should_throw_404() {
        // user courant
        User me = user;
        me.setId(1L);

        // item existant dans une collection à moi
        Collection myCollection = Collection.builder().name("Mine").owner(me).build();
        myCollection.setId(10L);

        Item item = Item.builder()
                .name("Old")
                .collection(myCollection)
                .status(ItemStatus.OTHER)
                .build();
        item.setId(100L);

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        // nouvelle collection qui appartient à quelqu’un d’autre
        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Collection otherCollection = Collection.builder().name("Other").owner(other).build();
        otherCollection.setId(999L);

        // le repo renvoie bien la collection, mais ownership check doit échouer
        when(collectionRepository.findById(999L)).thenReturn(Optional.of(otherCollection));

        ItemRequest req = new ItemRequest();
        req.setCollectionId(999L); // tentative de changer vers une collection non possédée
        req.setName("New");
        req.setDescription("New");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of());

        assertThatThrownBy(() -> itemService.updateItem(100L, req))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void createItem_category_not_found_should_throw_404() {
        Collection myCollection = Collection.builder().name("Mine").owner(user).build();
        myCollection.setId(10L);

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(myCollection));
        when(categoryRepository.findById(123L)).thenReturn(Optional.empty());

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setCategoryId(123L); // inexistant
        req.setName("Item");
        req.setTagIds(List.of());

        assertThatThrownBy(() -> itemService.createItem(req))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItem_tag_not_owned_should_be_filtered_out() {
        Collection myCollection = Collection.builder().name("Mine").owner(user).build();
        myCollection.setId(10L);

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(myCollection));

        Tag myTag = Tag.builder().name("Mine").owner(user).build();
        myTag.setId(1L);

        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Tag otherTag = Tag.builder().name("Other").owner(other).build();
        otherTag.setId(2L);

        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(myTag, otherTag));

        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("Item");
        req.setDescription("Desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of(1L, 2L)); // mélange : à moi + pas à moi

        ItemResponse res = itemService.createItem(req);

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getTags()).contains("Mine");
        assertThat(res.getTags()).doesNotContain("Other");
    }


}
