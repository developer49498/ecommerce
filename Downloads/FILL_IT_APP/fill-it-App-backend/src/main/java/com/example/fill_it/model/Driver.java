package com.example.fill_it.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    private String id;              // Firebase UID
    private String username;
    private String email;
    private String vehicleNumber;
    private String phoneNumber;
}
