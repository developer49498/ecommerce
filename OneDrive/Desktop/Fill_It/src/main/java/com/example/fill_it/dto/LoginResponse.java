package com.example.fill_it.dto;


import lombok.Data;

@Data
public class LoginResponse {
    private String idToken;
    private String refreshToken;
    private String localId;   // Firebase UID
    private String email;
    private String role;
}
