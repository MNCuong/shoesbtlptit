package com.example.shoes_store.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyInventoryDTO {
    private String month;
    private BigDecimal importAmount;
    private BigDecimal exportAmount;
}
