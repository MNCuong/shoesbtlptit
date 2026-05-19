package com.example.shoes_store.Service.Impl;


import com.example.shoes_store.Entity.PaymentTransaction;
import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Repo.PaymentRepository;
import com.example.shoes_store.Repo.UserRepo;
import com.example.shoes_store.Service.PaymentService;
import com.example.shoes_store.Service.UserService;
import com.example.shoes_store.config.VnPayConfig;
import com.example.shoes_store.dto.PayRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final VnPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    @Autowired
    private UserRepo userRepo;

    @Override
    public String getPay(HttpServletRequest request, PayRequest payRequest) {

        log.info("========== START PAYMENT ==========");
        log.info("PayRequest - amount_raw: {}", payRequest.getAmount_raw());
        log.info("PayRequest - bankCode: {}", payRequest.getBankCode());
        log.info("PayRequest - userEmail: {}", payRequest.getUserEmail());
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = vnPayConfig.getIpAddress(request);
        BigDecimal amount_raw = payRequest.getAmount_raw();
        BigDecimal amount_multiplied = amount_raw.multiply(BigDecimal.valueOf(100));

        long amount = amount_multiplied.longValue();
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        if (payRequest.getBankCode() != null && !payRequest.getBankCode().isEmpty()) {
            vnp_Params.put("vnp_BankCode", payRequest.getBankCode());
        }
        String orderInfo = "Khách hàng-" + user.getId() + " : " + user.getFullname().toUpperCase(Locale.ROOT) + " thanh toán đơn hàng: " + vnp_TxnRef + ", mã đặt dịch vụ - " + payRequest.getBookingId();
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("ETC/GMT+7"));
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.HOUR, 10);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        log.info("data:{}", query.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        log.info("query:{}", vnp_SecureHash);
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + query.toString();
        log.info("Payment URL: {}", paymentUrl);
        return paymentUrl;
    }

    public boolean verifySignature(Map<String, String> params, String secureHash) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        fieldNames.remove("vnp_SecureHash");
        try {
            StringBuilder sb = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    sb.append(fieldName);
                    sb.append('=');
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    if (itr.hasNext()) {
                        query.append('&');
                        sb.append('&');
                    }
                }
            }
            String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), sb.toString());
            log.info("secureHash{}-------------------" + secureHash);
            log.info("sb:{}", sb);
            log.info("query:{}", query);
            log.info("vnp_SecureHash:{}", vnp_SecureHash);
            return vnp_SecureHash.equals(secureHash);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String saveTransaction(Map<String, String> params) {
        try {
            if (params == null || !params.containsKey("vnp_SecureHash") || !params.containsKey("vnp_ResponseCode")) {
                return "Invalid Params";
            }
            log.info("stauts:{}", params.get("vnp_TransactionStatus"));
            boolean verify = verifySignature(params, params.get("vnp_SecureHash"));
            if (verify) {
                String orderInfo = params.get("vnp_OrderInfo");
                Pattern pattern = Pattern.compile("mã đặt dịch vụ - \\[(.*?)\\]");
                Matcher matcher = pattern.matcher(orderInfo);

                List<Long> bookingIds = new ArrayList<>();
                if (matcher.find()) {
                    String bookingIdsStr = matcher.group(1);
                    for (String id : bookingIdsStr.split(",")) {
                        bookingIds.add(Long.parseLong(id.trim()));
                    }
                    log.info("Booking IDs: {}", bookingIds);
                } else {
                    throw new Error("Không tìm thấy mã đặt dịch vụ");
                }
                String responseCode = params.get("vnp_ResponseCode");
                if ("00".equals(responseCode)) {
                    for (Long bookingId : bookingIds) {
                        log.info("start");
                        log.info("bookingId: {}", bookingId);

//                    }
                    }
                }
                saveTran(params);
                return "Success";
            } else {

                saveTran(params);
                return "Fail";
            }

        } catch (
                Exception e) {
            params.put("vnp_TransactionStatus", "99");
            log.info("status: {}", params.get("vnp_TransactionStatus"));
            saveTran(params);
            log.error("Payment error: {}", e.getMessage());
            throw new Error("Thanh toán thất bại");        }

    }

    @Transactional
    @Override
    public void saveTran(Map<String, String> params) {
        String status;
        if (Objects.equals(params.get("vnp_TransactionStatus"), "00")) {
            status = "Success";
        } else {
            status = "Fail";
        }
        if (paymentRepository.existsByTransactionNo(params.get("vnp_TransactionNo"))) {
            throw new Error("Giao dịch đã tồn tại");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(params.get("vnp_PayDate"), formatter);
        LocalDate payDate = localDateTime.toLocalDate();
        String orderInfo = params.get("vnp_OrderInfo");
        String typeBooking = orderInfo.substring(orderInfo.lastIndexOf("-") + 1);
        String userId = orderInfo.split("-")[1].split(":")[0];
        User user = (User) userRepo.findUserById(Long.parseLong(userId.trim()));
        paymentRepository.save(PaymentTransaction.builder().amount(Double.parseDouble(params.get("vnp_Amount"))).description(params.get("vnp_OrderInfo")).paymentMethod(params.get("vnp_CardType")).transactionDate(payDate).user(user).transactionNo(params.get("vnp_TransactionNo")).typeBooking(typeBooking).status(status).build());
    }

//    @Override
//    public String getPayKafka(PayRequest payRequest) {
//
//        User user = userService.findUserByEmail(payRequest.getUserEmail());
//        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
//        String vnp_IpAddr = vnPayConfig.getIpAddress();
//        BigDecimal amount = payRequest.getAmount_raw().multiply(BigDecimal.valueOf(100));
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", "2.1.0");
//        vnp_Params.put("vnp_Command", "pay");
//        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
//        vnp_Params.put("vnp_Amount", String.valueOf(amount));
//        if (payRequest.getBankCode() != null && !payRequest.getBankCode().isEmpty()) {
//            vnp_Params.put("vnp_BankCode", payRequest.getBankCode());
//        }
//        String orderInfo = "Khách hàng-" + user.getId() + " : " + user.getFullName().toUpperCase(Locale.ROOT) + " thanh toán đơn hàng: " + vnp_TxnRef + ", mã đặt dịch vụ - " + payRequest.getBookingId() + ", loại dịch vụ - " + payRequest.getTypeService();
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_OrderInfo", orderInfo);
//        vnp_Params.put("vnp_OrderType", "other");
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//        log.info("amountPaymentListener:{}", payRequest.getAmount_raw());
//
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("ETC/GMT+7"));
//        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
//        cld.add(Calendar.HOUR, 10);
//        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder query = new StringBuilder();
//        StringBuilder hashData = new StringBuilder();
//        Iterator itr = fieldNames.iterator();
//        while (itr.hasNext()) {
//            String fieldName = (String) itr.next();
//            String fieldValue = vnp_Params.get(fieldName);
//            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
//                //Build hash data
//                hashData.append(fieldName);
//                hashData.append('=');
//                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
//                //Build query
//                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
//                query.append('=');
//                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
//                if (itr.hasNext()) {
//                    query.append('&');
//                    hashData.append('&');
//                }
//            }
//        }
//        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
//        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
//        log.info("URLpayment:{}", vnPayConfig.getVnp_PayUrl() + "?" + query.toString());
//        return vnPayConfig.getVnp_PayUrl() + "?" + query.toString();
//    }

//    @Override
//    public Page<PaymentTransaction> getList(int page, int size, String search) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
//        if (search == null || search.isEmpty()) {
//            return paymentRepository.findAll(pageable);
//        } else {
//            return paymentRepository.findAllByTransactionNo(search, pageable);
//        }
//
//    }

//    @Override
//    public PaymentTransaction getPaymentDetail(Long id) {
//        return paymentRepository.findById(id).orElse(null);
//    }
//
//    public Map<Integer, Double> getRevenueByMonth(int year) {
//        return paymentRepository.getRevenueByMonth(year)
//                .stream()
//                .collect(Collectors.toMap(
//                        row -> ((Integer) row[0]),
//                        row -> ((Double) row[1])
//                ));
//    }
//
//    public Map<Integer, Double> getRevenueByDayInMonth(int month, int year) {
//        return paymentRepository.getRevenueByDayInMonth(month, year)
//                .stream()
//                .collect(Collectors.toMap(
//                        row -> ((Integer) row[0]),
//                        row -> ((Double) row[1])
//                ));
//    }
//
//    public Map<Integer, Double> getRevenueByQuarter(int quarter, int year) {
//        int startMonth = (quarter - 1) * 3 + 1;
//        int endMonth = startMonth + 2;
//        return paymentRepository.getRevenueByQuarter(startMonth, endMonth, year)
//                .stream()
//                .collect(Collectors.toMap(
//                        row -> ((Integer) row[0]),
//                        row -> ((Double) row[1])
//                ));
//    }

    @Override
    public PaymentTransaction findByVnp_TransactionNo(String vnp_TransactionNo) {
        return paymentRepository.findByTransactionNo(vnp_TransactionNo);
    }
}
