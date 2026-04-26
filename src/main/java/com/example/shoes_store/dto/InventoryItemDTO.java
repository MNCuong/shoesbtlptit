package com.example.shoes_store.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryItemDTO {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private String gender;
}
