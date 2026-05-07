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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

        // Xử lý null cho các tham số
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int currentQuarter = (quarter != null) ? quarter : (LocalDate.now().getMonthValue() - 1) / 3 + 1;

        switch (type) {
            case "month":
                startDate = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0);
                int lastDay = startDate.toLocalDate().lengthOfMonth();
                endDate = LocalDateTime.of(currentYear, currentMonth, lastDay, 23, 59, 59);
                break;
            case "quarter":
                int startMonth = (currentQuarter - 1) * 3 + 1;
                int endMonth = currentQuarter * 3;
                startDate = LocalDateTime.of(currentYear, startMonth, 1, 0, 0, 0);
                int lastDayOfEndMonth = LocalDateTime.of(currentYear, endMonth, 1, 0, 0, 0)
                        .toLocalDate().lengthOfMonth();
                endDate = LocalDateTime.of(currentYear, endMonth, lastDayOfEndMonth, 23, 59, 59);
                break;
            case "year":
                startDate = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
                endDate = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);
                break;
            default:
                startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                endDate = LocalDateTime.now();
        }

        // Tổng doanh thu
        BigDecimal totalRevenue = orderRepo.sumTotalAmountByDateRangeAndStatus(startDate, endDate, "DELIVERED");
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // ===== QUAN TRỌNG: LẤY DỮ LIỆU THỰC TẾ THAY VÌ SỐ 0 =====
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        if (type.equals("month")) {
            // Thống kê theo tuần trong tháng
            labels = Arrays.asList("Tuần 1 (1-7)", "Tuần 2 (8-14)", "Tuần 3 (15-21)", "Tuần 4 (22-28)", "Tuần 5 (29-31)");

            YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
            int daysInMonth = yearMonth.lengthOfMonth();

            // Tuần 1: ngày 1-7
            BigDecimal week1Revenue = getRevenueByDateRange(currentYear, currentMonth, 1, Math.min(7, daysInMonth));
            values.add(week1Revenue);

            // Tuần 2: ngày 8-14
            if (daysInMonth >= 8) {
                BigDecimal week2Revenue = getRevenueByDateRange(currentYear, currentMonth, 8, Math.min(14, daysInMonth));
                values.add(week2Revenue);
            } else {
                values.add(BigDecimal.ZERO);
            }

            // Tuần 3: ngày 15-21
            if (daysInMonth >= 15) {
                BigDecimal week3Revenue = getRevenueByDateRange(currentYear, currentMonth, 15, Math.min(21, daysInMonth));
                values.add(week3Revenue);
            } else {
                values.add(BigDecimal.ZERO);
            }

            // Tuần 4: ngày 22-28
            if (daysInMonth >= 22) {
                BigDecimal week4Revenue = getRevenueByDateRange(currentYear, currentMonth, 22, Math.min(28, daysInMonth));
                values.add(week4Revenue);
            } else {
                values.add(BigDecimal.ZERO);
            }

            // Tuần 5: ngày 29 đến hết tháng (nếu có)
            if (daysInMonth >= 29) {
                BigDecimal week5Revenue = getRevenueByDateRange(currentYear, currentMonth, 29, daysInMonth);
                values.add(week5Revenue);
            } else {
                values.add(BigDecimal.ZERO);
            }

        } else if (type.equals("quarter")) {
            // Thống kê theo tháng trong quý
            int startMonth = (currentQuarter - 1) * 3 + 1;
            labels = Arrays.asList("Tháng " + startMonth, "Tháng " + (startMonth + 1), "Tháng " + (startMonth + 2));

            for (int i = 0; i < 3; i++) {
                int m = startMonth + i;
                YearMonth ym = YearMonth.of(currentYear, m);
                LocalDateTime mStart = LocalDateTime.of(currentYear, m, 1, 0, 0, 0);
                LocalDateTime mEnd = LocalDateTime.of(currentYear, m, ym.lengthOfMonth(), 23, 59, 59);
                BigDecimal monthlyRevenue = orderRepo.sumTotalAmountByDateRangeAndStatus(mStart, mEnd, "DELIVERED");
                values.add(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
            }

        } else {
            // Thống kê theo quý trong năm
            labels = Arrays.asList("Quý 1", "Quý 2", "Quý 3", "Quý 4");

            for (int q = 1; q <= 4; q++) {
                int qStartMonth = (q - 1) * 3 + 1;
                int qEndMonth = q * 3;
                LocalDateTime qStart = LocalDateTime.of(currentYear, qStartMonth, 1, 0, 0, 0);
                YearMonth lastYm = YearMonth.of(currentYear, qEndMonth);
                LocalDateTime qEnd = LocalDateTime.of(currentYear, qEndMonth, lastYm.lengthOfMonth(), 23, 59, 59);
                BigDecimal quarterlyRevenue = orderRepo.sumTotalAmountByDateRangeAndStatus(qStart, qEnd, "DELIVERED");
                values.add(quarterlyRevenue != null ? quarterlyRevenue : BigDecimal.ZERO);
            }
        }

        log.info("===== THỐNG KÊ DOANH THU =====");
        log.info("Kỳ: {} - Năm: {} - Tổng: {}", type, currentYear, totalRevenue);
        log.info("Labels: {}", labels);
        log.info("Values: {}", values);
        log.info("=============================");

        return new StatisticsDTO(totalRevenue, labels, values);
    }

    // Thêm method helper để lấy doanh thu theo khoảng ngày trong tháng
    private BigDecimal getRevenueByDateRange(int year, int month, int startDay, int endDay) {
        LocalDateTime start = LocalDateTime.of(year, month, startDay, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, month, endDay, 23, 59, 59);
        BigDecimal revenue = orderRepo.sumTotalAmountByDateRangeAndStatus(start, end, "DELIVERED");
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    // ==================== THỐNG KÊ NHẬP/XUẤT KHO ====================

    public InventoryStatisticsDTO getInventoryStatistics(Integer year) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        LocalDateTime startDate = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        // Tổng nhập trong năm
        BigDecimal totalImport = importReceiptRepo.sumTotalAmountByDateRange(startDate, endDate);
        if (totalImport == null) totalImport = BigDecimal.ZERO;

        // Tổng xuất trong năm
        BigDecimal totalExport = exportReceiptRepo.sumTotalAmountByDateRange(startDate, endDate);
        if (totalExport == null) totalExport = BigDecimal.ZERO;

        // Dữ liệu theo tháng
        List<Object[]> monthlyImportData = importReceiptRepo.getMonthlyImportData(startDate, endDate);
        List<Object[]> monthlyExportData = exportReceiptRepo.getMonthlyExportData(startDate, endDate);

        // Log để debug
        log.info("Monthly import data: {}", monthlyImportData);
        log.info("Monthly export data: {}", monthlyExportData);

        // Tạo map tháng -> import amount
        Map<String, BigDecimal> importMap = new HashMap<>();
        if (monthlyImportData != null) {
            for (Object[] data : monthlyImportData) {
                if (data != null && data.length >= 2) {
                    String month = (String) data[0];
                    BigDecimal amount = (BigDecimal) data[1];
                    importMap.put(month, amount);
                    log.info("Import - Month: {}, Amount: {}", month, amount);
                }
            }
        }

        // Tạo map tháng -> export amount
        Map<String, BigDecimal> exportMap = new HashMap<>();
        if (monthlyExportData != null) {
            for (Object[] data : monthlyExportData) {
                if (data != null && data.length >= 2) {
                    String month = (String) data[0];
                    BigDecimal amount = (BigDecimal) data[1];
                    exportMap.put(month, amount);
                    log.info("Export - Month: {}, Amount: {}", month, amount);
                }
            }
        }

        // Tạo dữ liệu cho 12 tháng
        List<MonthlyInventoryDTO> monthlyData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String monthStr = String.format("%d-%02d", currentYear, i);
            BigDecimal importAmount = importMap.getOrDefault(monthStr, BigDecimal.ZERO);
            BigDecimal exportAmount = exportMap.getOrDefault(monthStr, BigDecimal.ZERO);

            log.info("Month {} - Import: {}, Export: {}", i, importAmount, exportAmount);

            monthlyData.add(new MonthlyInventoryDTO(
                    "Tháng " + i,
                    importAmount,
                    exportAmount
            ));
        }

        log.info("===== THỐNG KÊ NHẬP/XUẤT =====");
        log.info("Năm: {} - Tổng nhập: {} - Tổng xuất: {}", currentYear, totalImport, totalExport);
        log.info("==============================");

        return new InventoryStatisticsDTO(totalImport, totalExport, monthlyData);
    }}