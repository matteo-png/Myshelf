package com.myshelf.apiMyshelf.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.myshelf.apiMyshelf.model.Category;
import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.PurchasePlace;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.testsupport.PostgresContainerBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest extends PostgresContainerBase{

    @Autowired UserRepository userRepository;
    @Autowired CollectionRepository collectionRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired PurchasePlaceRepository purchasePlaceRepository;

    @Test
    void findByCollection_returns_items() {
        User u = userRepository.save(User.builder()
                .email("u@mail")
                .passwordHash("x")
                .displayName("U")
                .build());

        Collection c = collectionRepository.save(Collection.builder()
                .name("C")
                .owner(u)
                .build());

        itemRepository.save(Item.builder().name("I1").collection(c).build());
        itemRepository.save(Item.builder().name("I2").collection(c).build());

        assertThat(itemRepository.findByCollection(c)).hasSize(2);
    }

    @Test
    void findByCategory_returns_items() {
        User u = userRepository.save(User.builder()
                .email("u@mail").passwordHash("x").displayName("U").build());

        Collection c = collectionRepository.save(Collection.builder()
                .name("C").owner(u).build());

        Category cat = categoryRepository.save(Category.builder()
                .name("Cat").owner(u).build());

        itemRepository.save(Item.builder().name("I1").collection(c).category(cat).build());
        itemRepository.save(Item.builder().name("I2").collection(c).category(cat).build());

        assertThat(itemRepository.findByCategory(cat)).hasSize(2);
    }

    @Test
    void findByPurchasePlace_returns_items() {
        User u = userRepository.save(User.builder()
                .email("u@mail").passwordHash("x").displayName("U").build());

        Collection c = collectionRepository.save(Collection.builder()
                .name("C").owner(u).build());

        PurchasePlace place = purchasePlaceRepository.save(PurchasePlace.builder()
                .name("Amazon")
                .owner(u)
                .build());

        itemRepository.save(Item.builder().name("I1").collection(c).purchasePlace(place).build());
        itemRepository.save(Item.builder().name("I2").collection(c).purchasePlace(place).build());

        assertThat(itemRepository.findByPurchasePlace(place)).hasSize(2);
    }

}
