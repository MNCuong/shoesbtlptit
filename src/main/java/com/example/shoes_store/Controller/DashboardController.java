// controller/DashboardController.java
package com.example.shoes_store.Controller;

import com.example.shoes_store.Service.DashboardService;
import com.example.shoes_store.dto.DashboardStatsDTO;
import com.example.shoes_store.dto.RecentOrderDTO;
import com.example.shoes_store.dto.RevenueByMonthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/revenue-by-month")
    public ResponseEntity<RevenueByMonthDTO> getRevenueByMonth() {
        return ResponseEntity.ok(dashboardService.getRevenueByMonth());
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<List<RecentOrderDTO>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(limit));
    }
}