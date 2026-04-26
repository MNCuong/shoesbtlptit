package com.example.shoes_store.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_receipts")
@Data
public class ImportReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String receiptCode;

    private LocalDateTime importDate;
    private BigDecimal totalAmount;
    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "importReceipt", cascade = CascadeType.ALL)
    private List<ImportReceiptDetail> details = new ArrayList<>();
}