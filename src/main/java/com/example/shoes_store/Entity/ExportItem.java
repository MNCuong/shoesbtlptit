package com.example.shoes_store.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "export_items")
@Data
public class ExportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "export_id")
    private Export exportBill;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String size;
    private Integer quantity;
}