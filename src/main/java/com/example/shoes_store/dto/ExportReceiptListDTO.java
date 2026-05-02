// ExportReceiptListDTO.java
package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExportReceiptListDTO {
    private Long id;
    private String receiptCode;
    private LocalDateTime exportDate;
    private BigDecimal totalAmount;
    private String note;
    private LocalDateTime createdAt;
    private String storeName;
    private String createdByName;
    private String reason;
    private Integer itemCount;
}