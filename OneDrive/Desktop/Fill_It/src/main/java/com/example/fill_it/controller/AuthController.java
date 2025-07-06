package com.example.fill_it.controller;

import com.example.fill_it.dto.CustomerSignupRequest;
import com.example.fill_it.dto.DriverSignupRequest;
import com.example.fill_it.dto.LoginRequest;
import com.example.fill_it.dto.LoginResponse;
import com.example.fill_it.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup/driver")
    public ResponseEntity<?> registerDriver(@RequestBody DriverSignupRequest request) {
        try {
            return authService.registerDriver(request);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage("Driver signup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signup/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody CustomerSignupRequest request) {
        try {
            return authService.registerCustomer(request);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage("Customer signup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage("Login failed: " + e.getMessage()));
        }
    }


    @GetMapping("/")
    public ResponseEntity<String> demoHello() {
        String html = "<html><body><h1>Hello friends</h1></body></html>";
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
    }

    // Inner static class for error responses
    static class ErrorMessage {
        private String message;

        public ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @GetMapping("/user-details")
    public ResponseEntity<?> getUserDetails(@RequestParam String email) {
        return authService.getUserDetailsByEmail(email);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String email) {
        return authService.logoutUser(email);
    }

}
