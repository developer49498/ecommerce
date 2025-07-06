package com.example.fill_it.service;

import com.example.fill_it.dto.CustomerSignupRequest;
import com.example.fill_it.dto.DriverSignupRequest;
import com.example.fill_it.dto.LoginRequest;
import com.example.fill_it.dto.LoginResponse;
import com.example.fill_it.model.Customer;
import com.example.fill_it.model.Driver;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    public ResponseEntity<?> registerDriver(DriverSignupRequest request) throws Exception {
        // Step 1: Create user in Firebase Auth
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        String email = userRecord.getEmail();

        // Step 2: Save driver to Firestore with email as document ID
        Driver driver = new Driver(
                email, // set email as uid
                request.getName(),
                request.getEmail(),
                request.getVehicleNumber(),
                request.getPhoneNumber()
        );

        Firestore db = FirestoreClient.getFirestore();
        db.collection("drivers").document(email).set(driver); // 🔥 email used as doc ID

        Map<String,String> mapperData = Map.of("role", "driver");
        db.collection("mapper").document(email).set(mapperData);

        // ✅ Return response in JSON format
        Map<String, String> response = Map.of("message", "Driver registered successfully with email as UID: " + email);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> registerCustomer(CustomerSignupRequest request) throws Exception {
        // Step 1: Create user in Firebase Auth
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        String email = userRecord.getEmail();

        // Step 2: Save customer to Firestore with email as document ID
        Customer customer = new Customer(
                email, // set email as uid
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        Firestore db = FirestoreClient.getFirestore();
        db.collection("customers").document(email).set(customer); // 🔥 email used as doc ID

        Map<String,String> mapperData = Map.of("role", "customer");
        db.collection("mapper").document(email).set(mapperData);

        // ✅ Return response in JSON format
        Map<String, String> response = Map.of("message", "Customer registered successfully with email as UID: " + email);
        return ResponseEntity.ok(response);
    }

    public LoginResponse loginUser(LoginRequest request) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyDmQ-RLJLDcuCsGKuKHeXWXcX2pAC37LoU";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        body.put("returnSecureToken", true);

        LoginResponse loginResponse = restTemplate.postForObject(url, body, LoginResponse.class);

        // ✅ Fetch role from Firestore
        Firestore db = FirestoreClient.getFirestore();
        try {
            String email = request.getEmail();
            String role = db.collection("mapper")
                    .document(email)
                    .get()
                    .get()
                    .getString("role");

            loginResponse.setRole(role); // ✅ Set role in response
        } catch (Exception e) {
            loginResponse.setRole("unknown");
            // Optional: log error
            e.printStackTrace();
        }

        return loginResponse;
    }

    public ResponseEntity<?> getUserDetailsByEmail(String email) {
        Firestore db = FirestoreClient.getFirestore();

        try {
            // Step 1: Determine role using "mapper"
            String role = db.collection("mapper")
                    .document(email)
                    .get()
                    .get()
                    .getString("role");

            if (role == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No role mapping found for this email"));
            }

            // Step 2: Fetch user details from the correct collection
            Map<String, Object> userData;
            if (role.equals("driver")) {
                userData = db.collection("drivers")
                        .document(email)
                        .get()
                        .get()
                        .getData();
            } else if (role.equals("customer")) {
                userData = db.collection("customers")
                        .document(email)
                        .get()
                        .get()
                        .getData();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown role: " + role));
            }

            if (userData == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            // ✅ Combine user data with role info
            userData.put("role", role);
            return ResponseEntity.ok(userData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    public ResponseEntity<?> logoutUser(String email) {
        try {
            // Step 1: Get user by email
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);

            // Step 2: Revoke all refresh tokens (forces sign-out)
            FirebaseAuth.getInstance().revokeRefreshTokens(userRecord.getUid());

            Map<String, String> response = Map.of("message", "User logged out successfully (tokens revoked)");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to logout", "details", e.getMessage()));
        }
    }


}
