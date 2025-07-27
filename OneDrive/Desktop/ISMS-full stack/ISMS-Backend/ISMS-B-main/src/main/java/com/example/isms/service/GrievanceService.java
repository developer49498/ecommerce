package com.example.isms.service;

import com.example.isms.model.ForwardingChainEntry;
import com.example.isms.model.Grievance;
import com.example.isms.model.GrievanceMapEntry;
import com.example.isms.model.GrievanceWithStatus;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class GrievanceService {

    private final DatabaseReference dbRef;
    @Autowired
    private OfficialService officialService;


    public GrievanceService(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference();
    }

    // 🔐 Utility method to safely encode email for Firebase keys
    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    public String createGrievance(Grievance grievance, String studentId, String facultyNotationName) {
        // Step 1: Construct student email
        String studentEmail = studentId + "@iiit-bh.ac.in";
        String encodedStudentEmail = encodeEmail(studentEmail);

        // Step 2: Get faculty email from notation name
        String facultyEmail = officialService.getEmailByNotationName(facultyNotationName);
        if (facultyEmail == null) {
            throw new IllegalArgumentException("Invalid faculty notation name: " + facultyNotationName);
        }
        String encodedFacultyEmail = encodeEmail(facultyEmail);

        // Step 3: Proceed with grievance creation (unchanged)
        DatabaseReference grievanceRef = dbRef.child("grievances").push();
        String grievanceId = grievanceRef.getKey();

        grievance.setId(grievanceId);
        grievance.setTimestamp(System.currentTimeMillis());

        grievanceRef.setValueAsync(grievance);
        grievanceRef.child("resolveAccess").setValueAsync(facultyEmail);

        GrievanceMapEntry studentEntry = new GrievanceMapEntry(false, "pending");
        dbRef.child("grievance_map").child(encodedStudentEmail).child("grievances_for")
                .child(grievanceId).setValueAsync(studentEntry);

        GrievanceMapEntry facultyEntry = new GrievanceMapEntry(true, "pending");
        dbRef.child("grievance_map").child(encodedFacultyEmail).child("grievances_for")
                .child(grievanceId).setValueAsync(facultyEntry);

        return grievanceId;
    }

    // 🔹 Resolve a grievance
    public void resolveGrievance(String grievanceId, String resolverNotationName) {
        // Step 1: Get resolver email from notation name
        String resolverEmail = officialService.getEmailByNotationName(resolverNotationName);
        if (resolverEmail == null) {
            System.err.println("Invalid resolver notation name: " + resolverNotationName);
            return;
        }

        String encodedResolverEmail = encodeEmail(resolverEmail);

        DatabaseReference resolveAccessRef = dbRef.child("grievances").child(grievanceId).child("resolveAccess");

        resolveAccessRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String allowedEmail = encodeEmail(snapshot.getValue(String.class));

                if (encodedResolverEmail.equals(allowedEmail)) {
                    dbRef.child("grievances").child(grievanceId).child("finalStatus")
                            .setValueAsync("resolved");
                } else {
                    System.out.println("Access denied: " + resolverEmail + " is not allowed to resolve this grievance.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error reading resolveAccess: " + error.getMessage());
            }
        });
    }

    // 🔹 Forward a grievance
    // Updated method signature to accept forwardingNote
    public void forwardGrievance(String grievanceId, String fromNotationName, String toNotationName, String forwardingNote) {
        // Step 1: Resolve notation names to emails
        String fromEmail = officialService.getEmailByNotationName(fromNotationName);
        String toEmail = officialService.getEmailByNotationName(toNotationName);

        if (fromEmail == null || toEmail == null) {
            System.err.println("Invalid notation name(s): from=" + fromNotationName + ", to=" + toNotationName);
            return;
        }

        String encodedFromEmail = encodeEmail(fromEmail);
        String encodedToEmail = encodeEmail(toEmail);
        long now = System.currentTimeMillis();

        // Step 2: Get the grievance's 'from' email (the student who posted it)
        dbRef.child("grievances").child(grievanceId).child("from").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String studentEmail = snapshot.getValue(String.class);
                    if (!studentEmail.contains("@")) {
                        studentEmail += "@iiit-bh.ac.in";
                    }
                    String encodedStudentEmail = encodeEmail(studentEmail);

                    // Update grievance_map for student (revoke access)
                    GrievanceMapEntry studentEntry = new GrievanceMapEntry(false, "in-progress");
                    dbRef.child("grievance_map").child(encodedStudentEmail).child("grievances_for")
                            .child(grievanceId).setValueAsync(studentEntry);

                    // Add to forwarding chain
                    ForwardingChainEntry entry = new ForwardingChainEntry(fromEmail, toEmail, now, forwardingNote);
                    dbRef.child("grievances").child(grievanceId).child("forwarding_chain")
                            .setValueAsync(entry);

                    // Update grievance_map for sender (revoke access)
                    dbRef.child("grievance_map").child(encodedFromEmail).child("grievances_for")
                            .child(grievanceId).setValueAsync(new GrievanceMapEntry(false, "forwarded"));

                    // Update grievance_map for receiver (grant access)
                    dbRef.child("grievance_map").child(encodedToEmail).child("grievances_for")
                            .child(grievanceId).setValueAsync(new GrievanceMapEntry(true, "pending"));

                    // Update resolveAccess in grievance
                    dbRef.child("grievances").child(grievanceId).child("resolveAccess")
                            .setValueAsync(toEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error fetching grievance 'from' email: " + error.getMessage());
            }
        });
    }

    public CompletableFuture<List<Map<String, Object>>> getFullGrievancesByEmail(String email) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        String encodedEmail = encodeEmail(email);
        DatabaseReference grievancesForRef = dbRef.child("grievance_map").child(encodedEmail).child("grievances_for");

        grievancesForRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot grievanceMapSnapshot) {
                if (!grievanceMapSnapshot.exists()) {
                    future.complete(Collections.emptyList());
                    return;
                }

                List<Map<String, Object>> result = new ArrayList<>();
                List<String> grievanceIds = new ArrayList<>();
                Map<String, String> personalStatuses = new HashMap<>();

                for (DataSnapshot entry : grievanceMapSnapshot.getChildren()) {
                    String grievanceId = entry.getKey();
                    GrievanceMapEntry mapEntry = entry.getValue(GrievanceMapEntry.class);
                    grievanceIds.add(grievanceId);
                    personalStatuses.put(grievanceId, mapEntry.getStatus());
                }

                final int[] completed = {0};

                for (String grievanceId : grievanceIds) {
                    dbRef.child("grievances").child(grievanceId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot grievanceSnapshot) {
                            Map<String, Object> grievanceData = (Map<String, Object>) grievanceSnapshot.getValue();

                            if (grievanceData != null) {
                                String finalStatus = (String) grievanceData.get("finalStatus");
                                String status = "pending";

                                if ("resolved".equalsIgnoreCase(finalStatus)) {
                                    status = "resolved";
                                } else {
                                    status = personalStatuses.getOrDefault(grievanceId, "pending");
                                }

                                grievanceData.put("status", status);

                                Object rawTimestamp = grievanceData.get("timestamp");
                                if (rawTimestamp instanceof Long) {
                                    String formattedTime = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm:ss z", Locale.ENGLISH)
                                            .format(new Date((Long) rawTimestamp));
                                    grievanceData.put("formattedTimestamp", formattedTime);
                                }

                                result.add(grievanceData);
                            }

                            completed[0]++;
                            if (completed[0] == grievanceIds.size()) {
                                // ✅ Sort by timestamp descending
                                result.sort((a, b) -> {
                                    Long t1 = (Long) a.get("timestamp");
                                    Long t2 = (Long) b.get("timestamp");
                                    return Long.compare(t2 != null ? t2 : 0, t1 != null ? t1 : 0);
                                });

                                future.complete(result);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            completed[0]++;
                            if (completed[0] == grievanceIds.size()) {
                                future.complete(result);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Error fetching grievance_map: " + error.getMessage()));
            }
        });

        return future;
    }



    public CompletableFuture<List<Map<String, Object>>> getGrievancesByEmailAndStatus(String email, String statusFilter) {
        return getFullGrievancesByEmail(email).thenApply(fullGrievanceList -> {
            List<Map<String, Object>> filteredList = new ArrayList<>();

            for (Map<String, Object> grievance : fullGrievanceList) {
                String status = (String) grievance.get("status");
                if (status != null && status.equalsIgnoreCase(statusFilter)) {
                    filteredList.add(grievance);
                }
            }

            return filteredList;
        });
    }

}
