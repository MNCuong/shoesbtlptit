package com.example.shoes_store.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private String fullName;
    private String address;
    private String phone;
    private String paymentMethod;
}