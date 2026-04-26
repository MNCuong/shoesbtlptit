package com.example.shoes_store.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String features;
    private String brand;
    private BigDecimal price;
    private Integer stockQuantity;
    private String gender;
    private String ageGroup;
    private boolean active;
    private Long categoryId;
    private String imageUrl;
    private MultipartFile image;  // File upload từ máy tính
    private String existingImageUrl;
}