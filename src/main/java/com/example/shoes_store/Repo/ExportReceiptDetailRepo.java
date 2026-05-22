package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.ExportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ExportReceiptDetailRepo extends JpaRepository<ExportReceiptDetail, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ExportReceiptDetail erd WHERE erd.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}