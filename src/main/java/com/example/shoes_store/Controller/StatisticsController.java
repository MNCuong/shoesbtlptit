// controller/StatisticsController.java
package com.example.shoes_store.Controller;

import com.example.shoes_store.Service.StatisticsService;
import com.example.shoes_store.dto.InventoryStatisticsDTO;
import com.example.shoes_store.dto.StatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/revenue")
    public ResponseEntity<StatisticsDTO> getRevenueStatistics(
            @RequestParam String type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam Integer year) {
        return ResponseEntity.ok(statisticsService.getRevenueStatistics(type, month, quarter, year));
    }

    @GetMapping("/inventory")
    public ResponseEntity<InventoryStatisticsDTO> getInventoryStatistics(
            @RequestParam Integer year) {
        return ResponseEntity.ok(statisticsService.getInventoryStatistics(year));
    }
}