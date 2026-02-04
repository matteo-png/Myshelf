package com.myshelf.apiMyshelf.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.myshelf.apiMyshelf.model.Tag;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.testsupport.PostgresContainerBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TagRepositoryTest extends PostgresContainerBase{

    @Autowired UserRepository userRepository;
    @Autowired TagRepository tagRepository;

    @Test
    void findByOwner_returns_only_owner_tags() {
        User u1 = userRepository.save(User.builder().email("u1@mail").passwordHash("x").displayName("U1").build());
        User u2 = userRepository.save(User.builder().email("u2@mail").passwordHash("x").displayName("U2").build());

        tagRepository.save(Tag.builder().name("T1").owner(u1).build());
        tagRepository.save(Tag.builder().name("T2").owner(u2).build());

        assertThat(tagRepository.findByOwner(u1)).hasSize(1);
        assertThat(tagRepository.findByOwner(u2)).hasSize(1);
    }
}
