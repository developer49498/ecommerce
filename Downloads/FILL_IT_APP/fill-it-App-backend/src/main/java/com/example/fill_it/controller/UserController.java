package com.example.fill_it.controller;

import com.example.fill_it.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/customer/phone")
    public ResponseEntity<?> updateCustomerPhone(@RequestParam String email, @RequestParam String newPhone) {
        return userService.updateCustomerPhone(email, newPhone);
    }

    @PutMapping("/driver/phone")
    public ResponseEntity<?> updateDriverPhone(@RequestParam String email, @RequestParam String newPhone) {
        return userService.updateDriverPhone(email, newPhone);
    }

    @GetMapping("/customer/details")
    public ResponseEntity<?> getCustomer(@RequestParam String email) {
        return userService.getCustomerDetailsByEmail(email);
    }

    @GetMapping("/driver/details")
    public ResponseEntity<?> getDriver(@RequestParam String email) {
        return userService.getDriverDetailsByEmail(email);
    }

}
