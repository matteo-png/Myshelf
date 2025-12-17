package com.myshelf.apiMyshelf.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.testsupport.PostgresContainerBase;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CollectionRepositoryTest extends  PostgresContainerBase{

    
    @Autowired UserRepository userRepository;
    @Autowired CollectionRepository collectionRepository;

    @Test
    void findByOwner_returns_collections() {
        User user = User.builder().email("a@a.com").passwordHash("x").displayName("A").build();
        userRepository.save(user);

        collectionRepository.save(Collection.builder().name("C1").owner(user).build());

        assertThat(collectionRepository.findByOwner(user)).hasSize(1);
    }




}
