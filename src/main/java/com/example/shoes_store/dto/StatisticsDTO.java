package com.example.shoes_store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private BigDecimal totalRevenue;
    private List<String> labels;
    private List<BigDecimal> values;
}