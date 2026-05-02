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
    private String storeAddress;
    private LocalDateTime createdAt;
    private LocalDateTime importDate;
    private BigDecimal totalAmount;
    private String note;
    private List<ImportReceiptDetailDTO> details;
    private String createdByName;     // Tên người tạo phiếu
    private String supplierName;      // Tên nhà phân phối
    private String supplierAddress;   // Địa chỉ nhà phân phối (nếu cần)
}