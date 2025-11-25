package com.myshelf.apiMyshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myshelf.apiMyshelf.model.Category;
import com.myshelf.apiMyshelf.model.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwnerOrOwnerIsNull(User owner);
}