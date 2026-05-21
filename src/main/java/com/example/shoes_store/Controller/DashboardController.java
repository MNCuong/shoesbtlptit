// controller/DashboardController.java
package com.example.shoes_store.Controller;

import com.example.shoes_store.Repo.OrderDetailRepository;
import com.example.shoes_store.Service.DashboardService;
import com.example.shoes_store.dto.DashboardStatsDTO;
import com.example.shoes_store.dto.RecentOrderDTO;
import com.example.shoes_store.dto.RevenueByMonthDTO;
import com.example.shoes_store.dto.TopProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    @Autowired
    private OrderDetailRepository orderDetailRepository;

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

    @GetMapping("/top-products")
    @ResponseBody
    public List<Map<String, Object>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        LocalDate now = LocalDate.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        List<Object[]> results = orderDetailRepository.findTopSellingProductsByMonth(targetMonth, targetYear);
        List<Map<String, Object>> topProducts = new ArrayList<>();

        for (int i = 0; i < Math.min(limit, results.size()); i++) {
            Object[] row = results.get(i);
            Map<String, Object> product = new HashMap<>();

            // row[0] = p.id (Long/Integer)
            // row[1] = p.name (String)
            // row[2] = p.imageUrl (String)
            // row[3] = SUM(od.quantity) (Long/Integer)
            // row[4] = SUM(od.totalPrice) (BigDecimal)

            product.put("productId", ((Number) row[0]).longValue());
            product.put("productName", row[1] != null ? row[1].toString() : "Không có tên");
            product.put("imageUrl", row[2] != null ? row[2].toString() : "");
            product.put("totalQuantity", ((Number) row[3]).intValue());
            product.put("totalRevenue", (BigDecimal) row[4]);

            topProducts.add(product);
        }

        return topProducts;
    }
}