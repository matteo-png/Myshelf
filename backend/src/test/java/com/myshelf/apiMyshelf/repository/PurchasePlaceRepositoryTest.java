package com.myshelf.apiMyshelf.repository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.myshelf.apiMyshelf.model.PurchasePlace;
import com.myshelf.apiMyshelf.model.PurchasePlaceType;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.testsupport.PostgresContainerBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchasePlaceRepositoryTest extends PostgresContainerBase{

    @Autowired UserRepository userRepository;
    @Autowired PurchasePlaceRepository purchasePlaceRepository;

    @Test
    void findByOwner_returns_only_owner_places() {
        User u1 = userRepository.save(User.builder().email("u1@mail").passwordHash("x").displayName("U1").build());
        User u2 = userRepository.save(User.builder().email("u2@mail").passwordHash("x").displayName("U2").build());

        purchasePlaceRepository.save(PurchasePlace.builder()
                .name("Amazon").type(PurchasePlaceType.ONLINE).websiteUrl("https://amazon.fr").owner(u1).build());

        purchasePlaceRepository.save(PurchasePlace.builder()
                .name("Fnac").type(PurchasePlaceType.MAGASIN).websiteUrl("https://fnac.com").owner(u2).build());

        assertThat(purchasePlaceRepository.findByOwner(u1)).hasSize(1);
        assertThat(purchasePlaceRepository.findByOwner(u2)).hasSize(1);
    }
}
