package com.myshelf.apiMyshelf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myshelf.apiMyshelf.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}