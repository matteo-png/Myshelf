package com.myshelf.apiMyshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myshelf.apiMyshelf.model.PurchasePlace;
import com.myshelf.apiMyshelf.model.User;

public interface PurchasePlaceRepository extends JpaRepository<PurchasePlace, Long> {

    List<PurchasePlace> findByOwner(User owner);
}