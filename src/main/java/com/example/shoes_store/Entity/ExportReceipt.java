package com.example.shoes_store.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "export_receipts")
@Data
public class ExportReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String receiptCode;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private LocalDateTime exportDate;
    private BigDecimal totalAmount;
    private String reason;
    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "exportReceipt", cascade = CascadeType.ALL)
    private List<ExportReceiptDetail> details = new ArrayList<>();
}