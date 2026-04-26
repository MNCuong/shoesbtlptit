// ImportReceiptDTO.java
package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ImportReceiptDTO {
    private Long id;
    private String receiptCode;
//    private String storeName;
    private String storeAddress;
    private LocalDateTime createdAt;
    private LocalDateTime importDate;
    private BigDecimal totalAmount;
    private String note;
    private List<ImportReceiptDetailDTO> details;
}