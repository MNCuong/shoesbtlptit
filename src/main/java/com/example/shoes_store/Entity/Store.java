package com.example.shoes_store.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private boolean active;
}