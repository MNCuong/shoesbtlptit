package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.ImportReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportReceiptDetailRepo extends JpaRepository<ImportReceiptDetail, Long> {
    void deleteByProductId(Long productId);
}
