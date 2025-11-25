package com.myshelf.apiMyshelf;
/* 
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.ItemStatus;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.CollectionRepository;
import com.myshelf.apiMyshelf.repository.ItemRepository;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner demoData(
            UserRepository userRepository,
            CollectionRepository collectionRepository,
            ItemRepository itemRepository
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return; 
            }

            User user = User.builder()
                    .email("test@myshelf.local")
                    .passwordHash("password") 
                    .displayName("Test User")
                    .build();
            userRepository.save(user);

            Collection collection = Collection.builder()
                    .name("Collection principale")
                    .description("Ma première collection")
                    .owner(user)
                    .build();
            collectionRepository.save(collection);

            Item item = Item.builder()
                    .name("Mon premier objet")
                    .description("Un objet de test")
                    .collection(collection)
                    .estimatedValue(new BigDecimal("42.00"))
                    .purchaseDate(LocalDate.now())
                    .status(ItemStatus.OTHER)
                    .build();
            itemRepository.save(item);
        };
    }
}
*/