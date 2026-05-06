// dto/InventoryStatisticsDTO.java
package com.example.shoes_store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatisticsDTO {
    private BigDecimal totalImport;
    private BigDecimal totalExport;
    private List<MonthlyInventoryDTO> monthlyData;
}
