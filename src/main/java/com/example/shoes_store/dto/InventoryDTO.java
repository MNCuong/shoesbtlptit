package com.example.shoes_store.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryDTO {
    private String type;
    private Long supplierId;  // Thêm field này
    private Long storeId;
    private String importDate;
    private String exportDate;
    private String reason;
    private String note;
    private List<InventoryItemDTO> items;
    // ... getters/setters
}

