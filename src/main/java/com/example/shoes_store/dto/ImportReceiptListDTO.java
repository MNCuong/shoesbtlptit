package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ImportReceiptListDTO {
    private Long id;
    private String receiptCode;
    private LocalDateTime importDate;
    private BigDecimal totalAmount;
    private String note;
    private LocalDateTime createdAt;
    private String supplierName;  // Tên nhà phân phối
    private String createdByName;  // Tên người tạo
    private Integer itemCount;  // Số lượng sản phẩm trong phiếu
}