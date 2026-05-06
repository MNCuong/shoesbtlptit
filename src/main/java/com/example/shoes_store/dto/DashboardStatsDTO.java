package com.example.shoes_store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private BigDecimal monthlyRevenue;
    private Long totalOrders;
    private Integer newAccountsThisMonth;
    private Integer totalStores;
}