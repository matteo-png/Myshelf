package com.myshelf.apiMyshelf.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.testsupport.PostgresContainerBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryStatsTest extends PostgresContainerBase {
    @Autowired UserRepository userRepository;
    @Autowired CollectionRepository collectionRepository;
    @Autowired ItemRepository itemRepository;

    @Test
    void itemsByYear_groups_by_purchaseDate_year() {
        User u = userRepository.save(User.builder()
                .email("test@myshelf.local").passwordHash("x").displayName("U").build());

        Collection c = collectionRepository.save(Collection.builder().name("C").owner(u).build());

        itemRepository.save(Item.builder()
                .name("I1").collection(c)
                .purchaseDate(LocalDate.of(2025, 1, 10))
                .estimatedValue(new BigDecimal("10.00"))
                .build());

        itemRepository.save(Item.builder()
                .name("I2").collection(c)
                .purchaseDate(LocalDate.of(2025, 3, 5))
                .estimatedValue(new BigDecimal("5.00"))
                .build());

        itemRepository.save(Item.builder()
                .name("I3").collection(c)
                .purchaseDate(LocalDate.of(2024, 12, 1))
                .estimatedValue(new BigDecimal("20.00"))
                .build());

        List<Object[]> rows = itemRepository.itemsByYear("test@myshelf.local");

        // rows : {year(int), count, sum}
        assertThat(rows).isNotEmpty();
        // selon l'order by, 2024 puis 2025
        assertThat(rows.get(0)[0]).isEqualTo(2024);
        assertThat(((Number) rows.get(0)[1]).longValue()).isEqualTo(1L);
        assertThat((BigDecimal) rows.get(0)[2]).isEqualByComparingTo("20.00");

        assertThat(rows.get(1)[0]).isEqualTo(2025);
        assertThat(((Number) rows.get(1)[1]).longValue()).isEqualTo(2L);
        assertThat((BigDecimal) rows.get(1)[2]).isEqualByComparingTo("15.00");
    }

    @Test
    void itemsByMonth_groups_by_purchaseDate_month_for_year() {
        User u = userRepository.save(User.builder()
                .email("test@myshelf.local").passwordHash("x").displayName("U").build());

        Collection c = collectionRepository.save(Collection.builder().name("C").owner(u).build());

        itemRepository.save(Item.builder()
                .name("Jan").collection(c)
                .purchaseDate(LocalDate.of(2025, 1, 1))
                .estimatedValue(new BigDecimal("10.00"))
                .build());

        itemRepository.save(Item.builder()
                .name("Mar").collection(c)
                .purchaseDate(LocalDate.of(2025, 3, 1))
                .estimatedValue(new BigDecimal("5.00"))
                .build());

        itemRepository.save(Item.builder()
                .name("OtherYear").collection(c)
                .purchaseDate(LocalDate.of(2024, 3, 1))
                .estimatedValue(new BigDecimal("99.00"))
                .build());

        List<Object[]> rows = itemRepository.itemsByMonth("test@myshelf.local", 2025);

        // rows: {period("YYYY-MM"), count, sum}
        assertThat(rows).hasSize(2);

        assertThat(rows.get(0)[0]).isEqualTo("2025-01");
        assertThat(((Number) rows.get(0)[1]).longValue()).isEqualTo(1L);
        assertThat((BigDecimal) rows.get(0)[2]).isEqualByComparingTo("10.00");

        assertThat(rows.get(1)[0]).isEqualTo("2025-03");
        assertThat(((Number) rows.get(1)[1]).longValue()).isEqualTo(1L);
        assertThat((BigDecimal) rows.get(1)[2]).isEqualByComparingTo("5.00");
    }

}
