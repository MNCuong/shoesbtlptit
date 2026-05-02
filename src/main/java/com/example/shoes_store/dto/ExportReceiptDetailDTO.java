// ExportReceiptDetailDTO.java
package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExportReceiptDetailDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productGender;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String gender;
}