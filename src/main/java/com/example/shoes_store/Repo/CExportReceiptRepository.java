package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.ExportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CExportReceiptRepository extends JpaRepository<ExportReceipt, Long> {
    List<ExportReceipt> findAllByOrderByCreatedAtDesc();
    @Query("SELECT COALESCE(SUM(e.totalAmount), 0) FROM ExportReceipt e WHERE e.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalAmountByDateRange(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE_FORMAT(e.created_at, '%Y-%m') as month, COALESCE(SUM(e.total_amount), 0) as total " +
            "FROM export_receipts e " +
            "WHERE e.created_at BETWEEN :start AND :end " +
            "GROUP BY DATE_FORMAT(e.created_at, '%Y-%m') " +
            "ORDER BY month DESC", nativeQuery = true)
    List<Object[]> getMonthlyExportData(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
