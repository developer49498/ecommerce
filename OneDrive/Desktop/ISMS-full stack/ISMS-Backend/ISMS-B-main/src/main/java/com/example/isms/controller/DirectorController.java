package com.example.isms.controller;

import com.example.isms.model.Director;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/director")
public class DirectorController {

    // Save director details
    @PostMapping("/save_director_details")
    public ResponseEntity<String> saveDirectorDetails(@RequestBody Director director) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("director").document("director");

            // Explicitly delete the previous director (optional, not strictly needed)
            docRef.delete().get(); // Wait for deletion to complete

            // Save new director details
            ApiFuture<com.google.cloud.firestore.WriteResult> writeResult = docRef.set(director);

            return ResponseEntity.ok("Director saved at: " + writeResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Interrupted while saving director");
        } catch (ExecutionException e) {
            return ResponseEntity.status(500).body("Error saving director: " + e.getMessage());
        }
    }


    // Get director details
    @GetMapping("/get_director_details")
    public ResponseEntity<?> getDirectorDetails() {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("director").document("director");

            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Director director = document.toObject(Director.class);
                return ResponseEntity.ok(director);
            } else {
                return ResponseEntity.status(404).body("Director not found");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Interrupted while fetching director");
        } catch (ExecutionException e) {
            return ResponseEntity.status(500).body("Error fetching director: " + e.getMessage());
        }
    }
}
