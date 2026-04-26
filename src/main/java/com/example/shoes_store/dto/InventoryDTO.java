package com.example.shoes_store.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryDTO {
    private Long storeId;
    private String type;
    private String reason;
    private String note;
    private List<InventoryItemDTO> items;
}

