package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Order;
import com.example.shoes_store.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // 🔥 THÊM METHOD NÀY
    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status = :status")
    Long sumTotalAmountByDateRangeAndStatus(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("status") String status);

    @Query(value = "SELECT * FROM orders ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Order> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);
}