package com.myshelf.apiMyshelf.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
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
import com.myshelf.apiMyshelf.security.CurrentUserService;

class ItemServiceTest {

    @Mock ItemRepository itemRepository;
    @Mock CollectionRepository collectionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock PurchasePlaceRepository purchasePlaceRepository;
    @Mock TagRepository tagRepository;
    @Mock CurrentUserService currentUserService;
    @Mock ItemFileStorageService itemFileStorageService;

    @InjectMocks ItemService itemService;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .email("test@myshelf.local")
                .passwordHash("x")
                .displayName("Test")
                .build();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
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
        req.setTagIds(List.of());

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(collection));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        ItemResponse res = itemService.createItem(req);

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getName()).isEqualTo("Item 1");
        assertThat(res.getCollectionId()).isEqualTo(10L);

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_with_file_stores_metadata_and_public_url() {
        Collection collection = Collection.builder().name("C").owner(user).build();
        collection.setId(10L);

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("Item 1");
        req.setTagIds(List.of());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(collection));
        when(itemFileStorageService.save(anyLong(), any())).thenReturn(
                new ItemFileStorageService.StoredItemFile(
                        "stored-invoice.pdf",
                        "invoice.pdf",
                        "application/pdf",
                        Path.of("uploads", "stored-invoice.pdf")
                )
        );
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        ItemResponse res = itemService.createItem(req, file);

        assertThat(res.getFileName()).isEqualTo("invoice.pdf");
        assertThat(res.getFileContentType()).isEqualTo("application/pdf");
        assertThat(res.getFileUrl()).isEqualTo("/api/items/100/file");
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
        Collection collection = Collection.builder().name("C").owner(user).build();
        collection.setId(10L);

        Item item = Item.builder()
                .name("Old")
                .collection(collection)
                .status(ItemStatus.OTHER)
                .build();
        item.setId(100L);

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        Tag myTag = Tag.builder().name("Mine").owner(user).build();
        myTag.setId(1L);

        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Tag otherTag = Tag.builder().name("Other").owner(other).build();
        otherTag.setId(2L);

        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(myTag, otherTag));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("New");
        req.setDescription("New desc");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of(1L, 2L));

        ItemResponse res = itemService.updateItem(100L, req);

        assertThat(res.getName()).isEqualTo("New");
        assertThat(res.getTags()).contains("Mine");
        assertThat(res.getTags()).doesNotContain("Other");
    }

    @Test
    void updateItem_change_collection_not_owned_should_throw_404() {
        Collection myCollection = Collection.builder().name("Mine").owner(user).build();
        myCollection.setId(10L);

        Item item = Item.builder()
                .name("Old")
                .collection(myCollection)
                .status(ItemStatus.OTHER)
                .build();
        item.setId(100L);

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        Collection otherCollection = Collection.builder().name("Other").owner(other).build();
        otherCollection.setId(999L);

        when(collectionRepository.findById(999L)).thenReturn(Optional.of(otherCollection));

        ItemRequest req = new ItemRequest();
        req.setCollectionId(999L);
        req.setName("New");
        req.setDescription("New");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of());

        assertThatThrownBy(() -> itemService.updateItem(100L, req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createItem_category_not_found_should_throw_404() {
        Collection myCollection = Collection.builder().name("Mine").owner(user).build();
        myCollection.setId(10L);

        when(collectionRepository.findById(10L)).thenReturn(Optional.of(myCollection));
        when(categoryRepository.findById(123L)).thenReturn(Optional.empty());

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setCategoryId(123L);
        req.setName("Item");
        req.setTagIds(List.of());

        assertThatThrownBy(() -> itemService.createItem(req))
                .isInstanceOf(ResponseStatusException.class);

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
        req.setTagIds(List.of(1L, 2L));

        ItemResponse res = itemService.createItem(req);

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getTags()).contains("Mine");
        assertThat(res.getTags()).doesNotContain("Other");
    }

    @Test
    void deleteItem_removes_stored_file() {
        Collection collection = Collection.builder().name("C").owner(user).build();
        collection.setId(10L);

        Item item = Item.builder()
                .name("Old")
                .collection(collection)
                .status(ItemStatus.OTHER)
                .build();
        item.setId(100L);
        item.setFichierUrl("stored-invoice.pdf");

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        itemService.deleteItem(100L);

        verify(itemFileStorageService).deleteIfExists("stored-invoice.pdf", 1L);
        verify(itemRepository).delete(item);
    }

    @Test
    void updateItem_remove_file_clears_metadata() {
        Collection collection = Collection.builder().name("C").owner(user).build();
        collection.setId(10L);

        Item item = Item.builder()
                .name("Old")
                .collection(collection)
                .status(ItemStatus.OTHER)
                .build();
        item.setId(100L);
        item.setFichierUrl("stored-invoice.pdf");
        item.setFileName("invoice.pdf");
        item.setFileContentType("application/pdf");

        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemRequest req = new ItemRequest();
        req.setCollectionId(10L);
        req.setName("Old");
        req.setStatus(ItemStatus.OTHER);
        req.setTagIds(List.of());
        req.setRemoveFile(true);

        ItemResponse res = itemService.updateItem(100L, req);

        verify(itemFileStorageService).deleteIfExists("stored-invoice.pdf", 1L);
        assertThat(res.getFileUrl()).isNull();
        assertThat(res.getFileName()).isNull();
        assertThat(res.getFileContentType()).isNull();
    }
}
