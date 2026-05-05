package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreRepo extends JpaRepository<Store, Long> {
    @Query(value = "SELECT s.name, COALESCE(SUM(o.total_amount), 0) as revenue " +
            "FROM stores s " +
            "LEFT JOIN orders o ON o.store_id = s.id " +
            "AND o.status = 'DELIVERED' " +
            "AND o.created_at BETWEEN :start AND :end " +
            "GROUP BY s.id, s.name", nativeQuery = true)
    List<Object[]> getRevenueByStore(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);}
