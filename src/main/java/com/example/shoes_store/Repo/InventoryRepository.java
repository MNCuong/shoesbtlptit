package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByStoreIdAndProductIdAndSize(
            Long storeId, Long productId, String size);
}