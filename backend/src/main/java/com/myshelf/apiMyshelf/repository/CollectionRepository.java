package com.myshelf.apiMyshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myshelf.apiMyshelf.model.Collection;
import com.myshelf.apiMyshelf.model.User;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByOwner(User owner);
}