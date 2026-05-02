// ExportReceiptDTO.java
package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExportReceiptDTO {
    private Long id;
    private String receiptCode;
    private LocalDateTime exportDate;
    private BigDecimal totalAmount;
    private String note;
    private LocalDateTime createdAt;
    private String reason;
    private String storeName;
    private String storeAddress;
    private String createdByName;
    private List<ExportReceiptDetailDTO> details;
}