package com.example.isms.controller;

import com.example.isms.model.Faculty;
import com.example.isms.service.FacultyService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    FacultyService facultyService;

    private final Firestore firestore = FirestoreClient.getFirestore();
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String password) {
        try {
            // Firebase sign-in endpoint
            String firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyD9QKWx4Uphhma--QteVboSk6yACV8NSXs";

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("password", password);
            payload.put("returnSecureToken", true);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> firebaseResponse = restTemplate.exchange(firebaseAuthUrl, HttpMethod.POST, request, Map.class);

            if (firebaseResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = firebaseResponse.getBody();
                String uid = (String) body.get("localId");
                String emailPrefix = email.split("@")[0];

                // Skip special accounts
                if (!email.equalsIgnoreCase("director@iiit-bh.ac.in") && !email.equalsIgnoreCase("library@iiit-bh.ac.in")  && !email.equalsIgnoreCase("accountsoffice@iiit-bh.ac.in") && !email.equalsIgnoreCase("examoffice@iiit-bh.ac.in")) {
                    // Student check: second character is a digit
                    if (emailPrefix.length() > 1 && Character.isDigit(emailPrefix.charAt(1))) {
                        // ---- Student login flow ----
                        char programmeChar = emailPrefix.charAt(0);
                        String programme = switch (programmeChar) {
                            case 'b' -> "b-tech";
                            case 'a' -> "m-tech";
                            case 'c' -> "ph.d";
                            default -> null;
                        };

                        if (programme == null) {
                            return ResponseEntity.badRequest().body(Map.of("message", "Invalid programme code."));
                        }

                        int branchCode = Character.getNumericValue(emailPrefix.charAt(1));
                        Map<Integer, String> branchMap = Map.of(
                                1, "CSE",
                                2, "ETC",
                                3, "EEE",
                                4, "IT",
                                5, "CE"
                        );
                        String branchName = branchMap.getOrDefault(branchCode, "Unknown");
                        if (branchName.equals("Unknown")) {
                            return ResponseEntity.badRequest().body(Map.of("message", "Invalid branch code."));
                        }

                        String enrollmentYear = "20" + emailPrefix.substring(2, 4);
                        String studentId = emailPrefix;

                        DocumentReference docRef = firestore
                                .collection("branch")
                                .document(branchName)
                                .collection("programme")
                                .document(programme)
                                .collection("year")
                                .document(enrollmentYear)
                                .collection("students")
                                .document(studentId)
                                .collection("details")
                                .document("profile");

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastLogin", FieldValue.serverTimestamp());
                        docRef.set(updates, SetOptions.merge());

                    } else {
                        // ---- Faculty login flow ----
                        // ---- Faculty login flow ----
                        Faculty faculty = facultyService.getFacultyByEmail(email);

                        if (faculty != null) {
                            String facultyId = faculty.getId(); // e.g., F_CSE_01
                            String department = faculty.getDepartment().toUpperCase(); // e.g., "CSE"

                            DocumentReference docRef = firestore
                                    .collection("department")
                                    .document(department)
                                    .collection("faculty")
                                    .document(facultyId)
                                    .collection("details")
                                    .document("profile");

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("lastLogin", FieldValue.serverTimestamp());
                            docRef.set(updates, SetOptions.merge());
                        } else {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(Map.of("message", "Faculty record not found."));
                        }

                    }
                }

                // Return success
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("uid", uid);
                response.put("email", email);
                response.put("idToken", body.get("idToken"));

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid email or password"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }



    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiKey = "AIzaSyD9QKWx4Uphhma--QteVboSk6yACV8NSXs";

            // Step 1: Sign in to get idToken
            String signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
            Map<String, Object> signInPayload = Map.of(
                    "email", email,
                    "password", currentPassword,
                    "returnSecureToken", true
            );
            ResponseEntity<Map> signInResponse = restTemplate.postForEntity(signInUrl, signInPayload, Map.class);
            if (signInResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid current password"));
            }

            String idToken = (String) signInResponse.getBody().get("idToken");

            // Step 2: Send password update request
            String updateUrl = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=" + apiKey;
            Map<String, Object> updatePayload = Map.of(
                    "idToken", idToken,
                    "password", newPassword,
                    "returnSecureToken", true
            );
            ResponseEntity<Map> updateResponse = restTemplate.postForEntity(updateUrl, updatePayload, Map.class);
            if (updateResponse.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Failed to update password"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            // 1. Get the user by email
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
            String uid = user.getUid();

            // 2. Delete the user
            FirebaseAuth.getInstance().deleteUser(uid);

            // 3. Recreate user with new password
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword("changeme");

            FirebaseAuth.getInstance().createUser(request);

            return ResponseEntity.ok(Map.of(
                    "message", "User password reset successfully. New password is 'changeme'."
            ));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Firebase error: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Error: " + e.getMessage())
            );
        }
    }



    @PostMapping("/bulk-create")
    public ResponseEntity<?> bulkCreateUsers(@RequestBody List<Map<String, String>> users) {
        int successCount = 0;
        int failureCount = 0;

        for (Map<String, String> user : users) {
            try {
                FirebaseAuth.getInstance().createUser(
                        new UserRecord.CreateRequest()
                                .setEmail(user.get("email"))
                                .setPassword(user.get("password"))
                );
                successCount++;
            } catch (FirebaseAuthException e) {
                failureCount++;
            }
        }

        return ResponseEntity.ok("Successfully created " + successCount + " users. Failed: " + failureCount);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String email) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            FirebaseAuth.getInstance().deleteUser(userRecord.getUid());
            return ResponseEntity.ok("User deleted successfully.");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(404).body("Error deleting user: " + e.getMessage());
        }
    }





    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestParam String uid) {
        try {
            // Revoke all refresh tokens for the given user
            FirebaseAuth.getInstance().revokeRefreshTokens(uid);

            return ResponseEntity.ok(Map.of("message", "Logout successful. Refresh tokens revoked."));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error revoking tokens: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unexpected error: " + e.getMessage()));
        }
    }

}
