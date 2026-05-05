package com.example.shoes_store.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private Long id;
    private Long userId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private Integer itemCount;
}