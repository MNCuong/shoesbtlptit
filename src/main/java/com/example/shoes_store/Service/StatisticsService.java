// service/StatisticsService.java
package com.example.shoes_store.Service;

import com.example.shoes_store.Repo.CExportReceiptRepository;
import com.example.shoes_store.Repo.CImportReceiptRepository;
import com.example.shoes_store.Repo.OrderRepository;
import com.example.shoes_store.dto.InventoryStatisticsDTO;
import com.example.shoes_store.dto.MonthlyInventoryDTO;
import com.example.shoes_store.dto.StatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepo;
    private final CImportReceiptRepository importReceiptRepo;
    private final CExportReceiptRepository exportReceiptRepo;

    // ==================== THỐNG KÊ DOANH THU ====================

    public StatisticsDTO getRevenueStatistics(String type, Integer month, Integer quarter, Integer year) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (type) {
            case "month":
                startDate = LocalDateTime.of(year, month, 1, 0, 0, 0);
                int lastDay = startDate.toLocalDate().lengthOfMonth();
                endDate = LocalDateTime.of(year, month, lastDay, 23, 59, 59);
                break;
            case "quarter":
                int startMonth = (quarter - 1) * 3 + 1;
                int endMonth = quarter * 3;
                startDate = LocalDateTime.of(year, startMonth, 1, 0, 0, 0);
                int lastDayOfEndMonth = LocalDateTime.of(year, endMonth, 1, 0, 0, 0)
                        .toLocalDate().lengthOfMonth();
                endDate = LocalDateTime.of(year, endMonth, lastDayOfEndMonth, 23, 59, 59);
                break;
            case "year":
                startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
                endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
                break;
            default:
                startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                endDate = LocalDateTime.now();
        }

        // Tổng doanh thu
        BigDecimal totalRevenue = orderRepo.sumTotalAmountByDateRangeAndStatus(startDate, endDate, "DELIVERED");

        // Doanh thu theo cửa hàng
        List<Object[]> storeStats = orderRepo.getRevenueByStore(startDate, endDate);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (Object[] stat : storeStats) {
            labels.add((String) stat[0]);
            values.add((BigDecimal) stat[1]);
        }

        log.info("===== THỐNG KÊ DOANH THU =====");
        log.info("Kỳ: {} - Tổng: {}", type, totalRevenue);
        log.info("=============================");

        return new StatisticsDTO(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                labels,
                values
        );
    }

    // ==================== THỐNG KÊ NHẬP/XUẤT KHO ====================

    public InventoryStatisticsDTO getInventoryStatistics(Integer year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        // Tổng nhập trong năm
        BigDecimal totalImport = importReceiptRepo.sumTotalAmountByDateRange(startDate, endDate);

        // Tổng xuất trong năm
        BigDecimal totalExport = exportReceiptRepo.sumTotalAmountByDateRange(startDate, endDate);

        // Dữ liệu theo tháng
        List<Object[]> monthlyImportData = importReceiptRepo.getMonthlyImportData(startDate, endDate);
        List<Object[]> monthlyExportData = exportReceiptRepo.getMonthlyExportData(startDate, endDate);

        // Tạo map tháng -> import amount
        Map<String, BigDecimal> importMap = new HashMap<>();
        for (Object[] data : monthlyImportData) {
            String month = (String) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            importMap.put(month, amount);
        }

        // Tạo map tháng -> export amount
        Map<String, BigDecimal> exportMap = new HashMap<>();
        for (Object[] data : monthlyExportData) {
            String month = (String) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            exportMap.put(month, amount);
        }

        // Tạo dữ liệu cho 12 tháng
        List<MonthlyInventoryDTO> monthlyData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String monthStr = String.format("%d-%02d", year, i);
            monthlyData.add(new MonthlyInventoryDTO(
                    "Tháng " + i,
                    importMap.getOrDefault(monthStr, BigDecimal.ZERO),
                    exportMap.getOrDefault(monthStr, BigDecimal.ZERO)
            ));
        }

        log.info("===== THỐNG KÊ NHẬP/XUẤT =====");
        log.info("Năm: {} - Tổng nhập: {} - Tổng xuất: {}", year, totalImport, totalExport);
        log.info("==============================");

        return new InventoryStatisticsDTO(
                totalImport != null ? totalImport : BigDecimal.ZERO,
                totalExport != null ? totalExport : BigDecimal.ZERO,
                monthlyData
        );
    }
}