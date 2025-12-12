package com.myshelf.apiMyshelf.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.myshelf.apiMyshelf.model.Category;
import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;
import com.myshelf.apiMyshelf.model.PurchasePlace;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByCollection(Collection collection);
    List<Item> findByCategory(Category category);

    List<Item> findByPurchasePlace(PurchasePlace purchasePlace);

    @Query("""
        select count(i)
        from Item i
        where i.collection.owner.email = :email
    """)
    long countByOwnerEmail(String email);

    @Query("""
        select coalesce(sum(i.estimatedValue), 0)
        from Item i
        where i.collection.owner.email = :email
    """)
    BigDecimal sumEstimatedValueByOwnerEmail(String email);

    @Query("""
        select i.collection.name, count(i), coalesce(sum(i.estimatedValue), 0)
        from Item i
        where i.collection.owner.email = :email
        group by i.collection.name
        order by count(i) desc
    """)
    List<Object[]> itemsByCollection(String email);

    @Query("""
        select coalesce(i.category.name, 'Uncategorized'), count(i), coalesce(sum(i.estimatedValue), 0)
        from Item i
        where i.collection.owner.email = :email
        group by coalesce(i.category.name, 'Uncategorized')
        order by count(i) desc
    """)
    List<Object[]> itemsByCategory(String email);

    @Query("""
        select coalesce(i.purchasePlace.name, 'Unknown'), count(i), coalesce(sum(i.estimatedValue), 0)
        from Item i
        where i.collection.owner.email = :email
        group by coalesce(i.purchasePlace.name, 'Unknown')
        order by count(i) desc
    """)
    List<Object[]> itemsByPurchasePlace(String email);

    @Query("""
        select coalesce(cast(i.status as string), 'UNKNOWN'), count(i), coalesce(sum(i.estimatedValue), 0)
        from Item i
        where i.collection.owner.email = :email
        group by coalesce(cast(i.status as string), 'UNKNOWN')
        order by count(i) desc
    """)
    List<Object[]> itemsByStatus(String email);


    @Query(value = """
        select cast(extract(year from i.purchase_date) as int) as y,
               count(*) as c,
               coalesce(sum(i.estimated_value), 0) as v
        from items i
        join collections col on i.collection_id = col.id
        join users u on col.user_id = u.id
        where u.email = :email
          and i.purchase_date is not null
        group by y
        order by y
        """, nativeQuery = true)
    List<Object[]> itemsByYear(@Param("email") String email);

    @Query(value = """
        select to_char(date_trunc('month', i.purchase_date), 'YYYY-MM') as period,
               count(*) as c,
               coalesce(sum(i.estimated_value), 0) as v
        from items i
        join collections col on i.collection_id = col.id
        join users u on col.user_id = u.id
        where u.email = :email
          and i.purchase_date is not null
          and extract(year from i.purchase_date) = :year
        group by period
        order by period
        """, nativeQuery = true)
    List<Object[]> itemsByMonth(@Param("email") String email, @Param("year") int year);
}