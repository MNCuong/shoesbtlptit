package com.example.shoes_store.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private String orderCode;
    private String fullName;
    private String address;
    private String phone;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private String createdAtStr;  // Thêm field này
    private List<OrderDetailResponseDTO> orderDetails;
}