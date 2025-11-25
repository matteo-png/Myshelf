package com.myshelf.apiMyshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByCollection(Collection collection);
}