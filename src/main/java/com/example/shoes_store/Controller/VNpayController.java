package com.example.shoes_store.Controller;

import com.example.shoes_store.Service.PaymentService;
import com.example.shoes_store.dto.PayRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
@GetMapping("/vnpay-payment-result")
public String paymentResult(HttpServletRequest request, Model model) {

    // Lấy các tham số từ VNPay trả về
    Map<String, String> fields = new TreeMap<>();
    for (String key : request.getParameterMap().keySet()) {
        if (!key.equals("vnp_SecureHash")) {
            fields.put(key, request.getParameter(key));
        }
    }

    String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
    String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
    String vnp_Amount = request.getParameter("vnp_Amount");
    String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");
    String vnp_PayDate = request.getParameter("vnp_PayDate");
    String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
    String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Mã đơn hàng

    // Format số tiền (VNPay trả về đơn vị là đồng * 100)
    long amount = 0;
    if (vnp_Amount != null) {
        amount = Long.parseLong(vnp_Amount) / 100;
    }

    // Format thời gian
    String formattedTime = "";
    if (vnp_PayDate != null && vnp_PayDate.length() == 14) {
        try {
            String dateStr = vnp_PayDate.substring(0, 8);
            String timeStr = vnp_PayDate.substring(8);
            formattedTime = String.format("%s:%s:%s - %s/%s/%s",
                    timeStr.substring(0, 2),
                    timeStr.substring(2, 4),
                    timeStr.substring(4, 6),
                    dateStr.substring(6, 8),
                    dateStr.substring(4, 6),
                    dateStr.substring(0, 4)
            );
        } catch (Exception e) {
            formattedTime = vnp_PayDate;
        }
    }

    // Xác định trạng thái thanh toán
    String status = "failed";
    String title = "Thanh Toán Thất Bại";
    String message = "Giao dịch không thành công";
    String errorMessage = "";

    if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
        status = "success";
        title = "Thanh Toán Thành Công";
        message = "Cảm ơn bạn đã mua sắm tại Fashion Shop!";
    } else if ("24".equals(vnp_ResponseCode)) {
        errorMessage = "Giao dịch bị hủy bỏ bởi khách hàng.";
    } else if ("02".equals(vnp_ResponseCode)) {
        errorMessage = "Giao dịch bị lỗi từ ngân hàng phát hành thẻ.";
    } else {
        errorMessage = "Có lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại.";
    }

    // Add attributes to model
    model.addAttribute("status", status);
    model.addAttribute("title", title);
    model.addAttribute("message", message);
    model.addAttribute("errorMessage", errorMessage);
    model.addAttribute("orderCode", vnp_TxnRef);
    model.addAttribute("amount", amount);
    model.addAttribute("transactionTime", formattedTime);
    model.addAttribute("transactionNo", vnp_TransactionNo != null ? vnp_TransactionNo : "Không có");

    return "payment-result";
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
