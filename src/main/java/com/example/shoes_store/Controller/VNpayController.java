package com.example.shoes_store.Controller;

import com.example.shoes_store.Service.PaymentService;
import com.example.shoes_store.dto.PayRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vnpay")
public class VNpayController {
    @Autowired
    private PaymentService paymentService;

//    @GetMapping("/vnpay_return")
//    public ResponseEntity<Response<String>> getVNPayResponse(@RequestParam Map<String, String> params) {
//        String fullUrl = "/vnpay_return?" + params.entrySet().stream()
//                .map(entry -> entry.getKey() + "=" + entry.getValue())
//                .reduce((p1, p2) -> p1 + "&" + p2)
//                .orElse("");
//        log.info("getVNPayResponse: {}", fullUrl);
//        return ResponseEntity.success(paymentService.saveTransaction(params));
//    }
@GetMapping("/vnpay_return")
public void getVNPayResponse(@RequestParam Map<String, String> params,
                             HttpServletResponse response) throws IOException {
    try {
        String fullUrl = "/vnpay_return?" + params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");
        log.info("getVNPayResponse: {}", fullUrl);

        // Xử lý lưu transaction
        String result = paymentService.saveTransaction(params);

        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        double amount = Double.parseDouble(params.get("vnp_Amount")) / 100;
        String orderInfo = params.get("vnp_OrderInfo");

        // Redirect về frontend với kết quả
        String frontendUrl;
        if ("00".equals(responseCode)) {
            frontendUrl = String.format(
                    "http://localhost:8080/payment-success?transactionNo=%s&amount=%.0f&orderInfo=%s",
                    transactionNo, amount, URLEncoder.encode(orderInfo, "UTF-8")
            );
        } else {
            String message = getMessageByCode(responseCode);
            frontendUrl = String.format(
                    "http://localhost:8080/payment-failed?code=%s&message=%s",
                    responseCode, URLEncoder.encode(message, "UTF-8")
            );
        }

        log.info("Redirecting to: {}", frontendUrl);
        response.sendRedirect(frontendUrl);

    } catch (Exception e) {
        log.error("Error processing VNPay return: {}", e.getMessage());
        response.sendRedirect("http://localhost:8080/payment-failed?message=" +
                URLEncoder.encode(e.getMessage(), "UTF-8"));
    }
}

    private String getMessageByCode(String code) {
        if ("00".equals(code)) return "Thanh toán thành công";
        if ("24".equals(code)) return "Người dùng hủy thanh toán";
        return "Thanh toán thất bại";
    }


    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> request,
                                           HttpServletRequest httpRequest) {
        try {
            log.info("Received create-payment request: {}", request);

            // Kiểm tra và lấy giá trị an toàn
            String orderId = request.get("orderId") != null ? request.get("orderId").toString() : null;
            String amount = request.get("amount") != null ? request.get("amount").toString() : null;
            String orderInfo = request.get("orderInfo") != null ? request.get("orderInfo").toString() : null;
            String bookingId = request.get("orderId") != null ? request.get("orderId").toString() : orderId;

            // Validate dữ liệu
            if (orderId == null || orderId.isEmpty()) {
                log.error("orderId is null or empty");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Thiếu orderId"
                ));
            }

            if (amount == null || amount.isEmpty()) {
                log.error("amount is null or empty");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Thiếu amount"
                ));
            }

            if (orderInfo == null || orderInfo.isEmpty()) {
                log.error("orderInfo is null or empty");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Thiếu orderInfo"
                ));
            }



            log.info("Creating payment - orderId: {}, amount: {}, orderInfo: {}",
                    orderId, amount, orderInfo);

            // Tạo PayRequest
            PayRequest payRequest = new PayRequest();
            payRequest.setAmount_raw(new BigDecimal(amount));
            payRequest.setBookingId(bookingId);


            // Tạo payment URL
            String paymentUrl = paymentService.getPay(httpRequest, payRequest);

            log.info("Payment URL created: {}", paymentUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
