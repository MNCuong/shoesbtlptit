package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
        List<ImportReceipt> findAllByOrderByCreatedAtDesc();

}
