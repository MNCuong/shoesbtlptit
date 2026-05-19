package com.example.shoes_store.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PayRequest {
    BigDecimal amount_raw;
    String bankCode;
    String bookingId;
    String userEmail;
}