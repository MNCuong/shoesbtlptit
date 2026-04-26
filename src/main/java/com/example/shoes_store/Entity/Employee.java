package com.example.shoes_store.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private String fullName;
    private String phone;
    private String email;
    private String position;
    private LocalDate hireDate;
    private String status;
    private Long userId; // FK to User
}