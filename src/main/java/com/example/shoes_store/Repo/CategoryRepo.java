package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    List<Category> findByActive(boolean active);
    @Query("SELECT p.id FROM Product p WHERE p.name = :name")
    Long findIdByName(@Param("name") Long name);
}
