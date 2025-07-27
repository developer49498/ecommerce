package com.example.fill_it.dto;

import lombok.Data;

@Data
public class CustomerSignupRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
}
