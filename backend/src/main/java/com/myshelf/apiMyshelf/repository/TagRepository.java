package com.myshelf.apiMyshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myshelf.apiMyshelf.model.Tag;
import com.myshelf.apiMyshelf.model.User;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByOwner(User owner);

    long countByOwnerEmail(String email);

}