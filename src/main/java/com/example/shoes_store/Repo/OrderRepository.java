package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Order;
import com.example.shoes_store.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // 🔥 THÊM METHOD NÀY
    List<Order> findAllByOrderByCreatedAtDesc();


    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status = 'DELIVERED'")
    BigDecimal sumTotalAmountByDateRangeAndStatus(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

    @Query(value = "SELECT YEAR(o.created_at) as year, MONTH(o.created_at) as month, COALESCE(SUM(o.total_amount), 0) as revenue " +
            "FROM orders o " +
            "WHERE o.created_at BETWEEN :startDate AND :endDate " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY YEAR(o.created_at), MONTH(o.created_at) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT * FROM orders ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Order> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

    @Query(value = "SELECT s.name, COALESCE(SUM(o.total_amount), 0) as revenue " +
            "FROM stores s " +
            "LEFT JOIN orders o ON o.store_id = s.id " +
            "AND o.status = 'DELIVERED' " +
            "AND o.created_at BETWEEN :start AND :end " +
            "GROUP BY s.id, s.name", nativeQuery = true)
    List<Object[]> getRevenueByStore(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);


    // Thêm method này vào OrderRepository
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = :status")
    BigDecimal sumTotalAmountByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  @Param("status") String status);

}