package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.ExportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CExportReceiptRepository extends JpaRepository<ExportReceipt, Long> {
    List<ExportReceipt> findAllByOrderByCreatedAtDesc();
}
