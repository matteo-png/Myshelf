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

import com.myshelf.apiMyshelf.dto.purchaseplace.PurchasePlaceRequest;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.PurchasePlace;
import com.myshelf.apiMyshelf.model.PurchasePlaceType;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.PurchasePlaceRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

class PurchasePlaceServiceTest {

    @Mock PurchasePlaceRepository purchasePlaceRepository;
    @Mock UserRepository userRepository;
    @Mock ItemRepository itemRepository;

    @InjectMocks PurchasePlaceService purchasePlaceService;

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
    void createPurchasePlace_ok() {
        PurchasePlaceRequest req = new PurchasePlaceRequest();
        req.setName("Amazon");
        req.setType(PurchasePlaceType.ONLINE);
        req.setWebsiteUrl("https://amazon.fr");

        when(purchasePlaceRepository.save(any(PurchasePlace.class))).thenAnswer(inv -> {
            PurchasePlace p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        var res = purchasePlaceService.createPurchasePlace(req);

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getName()).isEqualTo("Amazon");
        assertThat(res.getType()).isEqualTo(PurchasePlaceType.ONLINE);
    }

    @Test
    void updatePurchasePlace_not_owner_should_throw_404() {
        User other = User.builder().email("other@mail").passwordHash("x").displayName("O").build();
        other.setId(2L);

        PurchasePlace place = PurchasePlace.builder()
                .name("Old")
                .type(PurchasePlaceType.OTHER)
                .owner(other)
                .build();
        place.setId(5L);

        when(purchasePlaceRepository.findById(5L)).thenReturn(Optional.of(place));

        PurchasePlaceRequest req = new PurchasePlaceRequest();
        req.setName("New");
        req.setType(PurchasePlaceType.MAGASIN);
        req.setWebsiteUrl("https://example.com");

        assertThatThrownBy(() -> purchasePlaceService.updatePurchasePlace(5L, req))
                .isInstanceOf(ResponseStatusException.class);

        verify(purchasePlaceRepository, never()).save(any());
    }

    @Test
    void deletePurchasePlace_used_by_items_should_throw_400() {
        PurchasePlace place = PurchasePlace.builder()
                .name("Used")
                .type(PurchasePlaceType.ONLINE)
                .owner(user)
                .build();
        place.setId(7L);

        when(purchasePlaceRepository.findById(7L)).thenReturn(Optional.of(place));
        when(itemRepository.findByPurchasePlace(place)).thenReturn(List.of(
                Item.builder().name("I1").build()
        ));

        ResponseStatusException ex = catchThrowableOfType(
                () -> purchasePlaceService.deletePurchasePlace(7L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(purchasePlaceRepository, never()).delete(any());
    }

    @Test
    void deletePurchasePlace_not_used_should_delete() {
        PurchasePlace place = PurchasePlace.builder()
                .name("Free")
                .type(PurchasePlaceType.ONLINE)
                .owner(user)
                .build();
        place.setId(8L);

        when(purchasePlaceRepository.findById(8L)).thenReturn(Optional.of(place));
        when(itemRepository.findByPurchasePlace(place)).thenReturn(List.of());

        purchasePlaceService.deletePurchasePlace(8L);

        verify(purchasePlaceRepository).delete(place);
    }
}
