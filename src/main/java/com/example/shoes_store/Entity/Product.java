package com.example.shoes_store.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String features;
    private String imageUrl;
    private String brand;
    private BigDecimal price;
    private Integer stockQuantity;
    private String gender; // BOY, GIRL, UNISEX
    private String ageGroup; // 0-2, 3-5, 6-10
    private boolean active;
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    public Product() {
    }
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportReceiptDetail> importReceiptDetails = new ArrayList<>();
     @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExportReceipt> exportReceiptDetails = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
}
