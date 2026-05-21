package com.example.shoes_store.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer totalQuantity;
    private BigDecimal totalRevenue;
}