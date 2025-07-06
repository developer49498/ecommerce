package com.example.fill_it.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    public ResponseEntity<?> updateCustomerPhone(String email, String newPhoneNumber) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("customers").document(email);

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("phoneNumber", newPhoneNumber);

            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get(); // Wait for update to complete

            return ResponseEntity.ok(Map.of("message", "Customer phone number updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update customer phone number", "details", e.getMessage()));
        }
    }

    public ResponseEntity<?> updateDriverPhone(String email, String newPhoneNumber) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("drivers").document(email);

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("phoneNumber", newPhoneNumber);

            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get(); // Wait for update to complete

            return ResponseEntity.ok(Map.of("message", "Driver phone number updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update driver phone number", "details", e.getMessage()));
        }
    }

    public ResponseEntity<?> getCustomerDetailsByEmail(String email) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("customers").document(email);

        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return ResponseEntity.ok(document.getData());
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Customer not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch customer details", "details", e.getMessage()));
        }
    }

    public ResponseEntity<?> getDriverDetailsByEmail(String email) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("drivers").document(email);

        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return ResponseEntity.ok(document.getData());
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Driver not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch driver details", "details", e.getMessage()));
        }
    }
}
