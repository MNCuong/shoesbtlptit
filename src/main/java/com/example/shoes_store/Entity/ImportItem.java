package com.example.shoes_store.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "import_items")
@Data
public class ImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "import_id")
    private Import importBill;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String size;
    private Integer quantity;
    private BigDecimal price;
}