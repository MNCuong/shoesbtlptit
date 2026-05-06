package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
        List<ImportReceipt> findAllByOrderByCreatedAtDesc();

        @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM ImportReceipt i WHERE i.createdAt BETWEEN :start AND :end")
        BigDecimal sumTotalAmountByDateRange(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

        @Query(value = "SELECT DATE_FORMAT(i.created_at, '%Y-%m') as month, COALESCE(SUM(i.total_amount), 0) as total " +
                "FROM import_receipts i " +
                "WHERE i.created_at BETWEEN :start AND :end " +
                "GROUP BY DATE_FORMAT(i.created_at, '%Y-%m') " +
                "ORDER BY month DESC", nativeQuery = true)
        List<Object[]> getMonthlyImportData(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}
