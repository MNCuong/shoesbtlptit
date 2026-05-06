// service/DashboardService.java
package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.Order;
import com.example.shoes_store.Repo.OrderRepository;
import com.example.shoes_store.Repo.UserRepo;
import com.example.shoes_store.Repo.StoreRepo;
import com.example.shoes_store.dto.DashboardStatsDTO;
import com.example.shoes_store.dto.RecentOrderDTO;
import com.example.shoes_store.dto.RevenueByMonthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepo userRepository;
    private final StoreRepo storeRepository;

    public DashboardStatsDTO getDashboardStats() {
        // Doanh thu tháng này (chỉ tính đơn hàng đã giao - DELIVERED)
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        BigDecimal monthlyRevenue = orderRepository.sumTotalAmountByDateRangeAndStatus(startOfMonth, endOfMonth);

        // Tổng đơn hàng
        Long totalOrders = orderRepository.count();

        // Tài khoản mới trong tháng
        Integer newAccounts = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);

        // Tổng số cửa hàng
        Integer totalStores = (int) storeRepository.count();

        return new DashboardStatsDTO(
                monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO,
                totalOrders != null ? totalOrders : 0L,
                newAccounts != null ? newAccounts : 0,
                totalStores != null ? totalStores : 0
        );
    }

    public RevenueByMonthDTO getRevenueByMonth() {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusMonths(11).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).toLocalDate().atTime(23, 59, 59);

        // Lấy doanh thu theo tháng từ database
        List<Object[]> monthlyRevenue = orderRepository.getMonthlyRevenue(startDate, endDate);

        // Tạo map tháng -> doanh thu
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenue) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            BigDecimal revenue = (BigDecimal) row[2];
            String key = month + "/" + year;
            revenueMap.put(key, revenue);
        }

        // Tạo labels và values cho 12 tháng gần nhất
        for (int i = 11; i >= 0; i--) {
            LocalDateTime date = now.minusMonths(i);
            int month = date.getMonth().getValue();
            int year = date.getYear();
            String label = "Tháng " + month + "/" + year;
            labels.add(label);
            values.add(revenueMap.getOrDefault(month + "/" + year, BigDecimal.ZERO));
        }

        return new RevenueByMonthDTO(labels, values);
    }

    public List<RecentOrderDTO> getRecentOrders(int limit) {
        return orderRepository.findTopNByOrderByCreatedAtDesc(limit)
                .stream()
                .map(this::convertToRecentOrderDTO)
                .collect(Collectors.toList());
    }

    private RecentOrderDTO convertToRecentOrderDTO(Order order) {
        String customerName = order.getFullName();
        if (order.getUser() != null && order.getUser().getFullname() != null) {
            customerName = order.getUser().getFullname();
        }

        return new RecentOrderDTO(
                order.getId(),
                order.getOrderCode(),
                customerName,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}