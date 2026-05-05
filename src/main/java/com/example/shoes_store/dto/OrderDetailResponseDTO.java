package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDetailResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String size;
    private String gender;
}