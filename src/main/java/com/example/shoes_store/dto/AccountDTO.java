package com.example.shoes_store.dto;

import lombok.Data;

@Data
public class AccountDTO {
    private Long id;
    private String fullname;
    private String username;
    private String password;
    private String email;
    private String role;


}