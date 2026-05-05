package com.example.shoes_store.dto;

import lombok.Data;

@Data
public class CartRequestDTO {
    private Long productId;
    private Integer quantity;
    private String size;
}