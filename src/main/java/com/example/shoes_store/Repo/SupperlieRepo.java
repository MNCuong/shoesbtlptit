package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupperlieRepo extends JpaRepository<Supplier, Long> {
}
