package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("SELECT p.id, p.name, p.imageUrl, " +
            "SUM(od.quantity), SUM(od.totalPrice) " +
            "FROM OrderDetail od " +
            "JOIN od.product p " +
            "JOIN od.order o " +
            "WHERE FUNCTION('MONTH', o.createdAt) = :month " +
            "AND FUNCTION('YEAR', o.createdAt) = :year " +
            "AND o.status != 'CANCELLED' " +
            "GROUP BY p.id, p.name, p.imageUrl " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findTopSellingProductsByMonth(@Param("month") int month, @Param("year") int year);
}