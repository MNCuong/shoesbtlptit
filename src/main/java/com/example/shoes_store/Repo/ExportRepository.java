package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Export;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportRepository extends JpaRepository<Export, Long> {}