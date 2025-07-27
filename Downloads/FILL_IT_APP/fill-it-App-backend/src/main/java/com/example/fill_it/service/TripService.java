package com.example.fill_it.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TripService {

    private final DatabaseReference tripsRef;

    public TripService() {
        this.tripsRef = FirebaseDatabase.getInstance().getReference("trips");
    }


    public CompletableFuture<String> createTrip(String customerEmail, String from, String to,
                                                double fromLat, double fromLon,
                                                double toLat, double toLon,
                                                String date)  {
        CompletableFuture<String> future = new CompletableFuture<>();
        Firestore db = FirestoreClient.getFirestore();

        db.collection("customers").document(customerEmail).get().addListener(() -> {
            try {
                DocumentSnapshot snapshot = db.collection("customers")
                        .document(customerEmail)
                        .get()
                        .get();

                if (!snapshot.exists()) {
                    future.completeExceptionally(new RuntimeException("Customer not found"));
                    return;
                }

                // ✅ Use already provided fromLat and fromLon
                String customerName = snapshot.getString("username");
                String customerPhone = snapshot.getString("phoneNumber");

                Map<String, Object> tripData = new HashMap<>();
                tripData.put("customerName", customerName);
                tripData.put("customerPhone", customerPhone);
                tripData.put("customerEmail", customerEmail);
                tripData.put("from", from);
                tripData.put("to", to);
                tripData.put("date", date);
                tripData.put("status", "pending");
                tripData.put("fromLat", fromLat);
                tripData.put("fromLon", fromLon);

                String tripId = tripsRef.push().getKey();
                if (tripId == null) {
                    future.completeExceptionally(new RuntimeException("Failed to generate trip ID"));
                    return;
                }

                tripData.put("id", tripId);

                tripsRef.child(tripId).setValueAsync(tripData)
                        .addListener(() -> future.complete(tripId), Runnable::run);

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, Runnable::run);

        return future;
    }

    // ✅ Accept Trip
    public CompletableFuture<String> acceptTrip(String tripId, String driverEmail) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Firestore db = FirestoreClient.getFirestore();

        db.collection("drivers").document(driverEmail).get().addListener(() -> {
            try {
                DocumentSnapshot snapshot = db.collection("drivers")
                        .document(driverEmail)
                        .get()
                        .get();

                if (!snapshot.exists()) {
                    future.completeExceptionally(new RuntimeException("Driver not found"));
                    return;
                }

                String driverName = snapshot.getString("username");
                String driverPhone = snapshot.getString("phoneNumber");

                Map<String, Object> updates = new HashMap<>();
                updates.put("driverName", driverName);
                updates.put("driverPhone", driverPhone);
                updates.put("driverEmail", driverEmail);
                updates.put("status", "accepted");
                updates.put("vehicleNumber",snapshot.getString("vehicleNumber"));

                // ✅ Use updateChildrenAsync to **merge** instead of overwrite
                tripsRef.child(tripId).updateChildrenAsync(updates)
                        .addListener(() -> future.complete("Trip accepted without overwriting"), Runnable::run);

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, Runnable::run);

        return future;
    }

    // ✅ Complete Trip
    public CompletableFuture<String> completeTrip(String tripId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        Map<String, Object> update = Map.of("status", "completed");

        tripsRef.child(tripId).updateChildrenAsync(update)
                .addListener(() -> future.complete("Trip marked as completed"), Runnable::run);

        return future;
    }

    public CompletableFuture<Map<String, Object>> getTripsByCustomerEmail(String customerEmail) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        tripsRef.orderByChild("customerEmail").equalTo(customerEmail)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> result = new HashMap<>();
                        for (DataSnapshot tripSnap : dataSnapshot.getChildren()) {
                            Map<String, Object> tripData = (Map<String, Object>) tripSnap.getValue();
                            if (tripData != null) {
                                // Process from and to fields
                                String from = (String) tripData.get("from");
                                String to = (String) tripData.get("to");
                                if (from != null) {
                                    if (from.contains(" ")) {
                                        from = from.split(" ")[0];
                                    }
                                    if (from.endsWith(",")) {
                                        from = from.substring(0, from.length() - 1);
                                    }
                                    tripData.put("from", from);
                                }
                                if (to != null) {
                                    if (to.contains(" ")) {
                                        to = to.split(" ")[0];
                                    }
                                    if (to.endsWith(",")) {
                                        to = to.substring(0, to.length() - 1);
                                    }
                                    tripData.put("to", to);
                                }
                                result.put(tripSnap.getKey(), tripData);
                            }
                        }
                        future.complete(result);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }


    public CompletableFuture<Map<String, Object>> getTripsByDriverEmail(String driverEmail) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        tripsRef.orderByChild("driverEmail").equalTo(driverEmail)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> result = new HashMap<>();
                        for (DataSnapshot tripSnap : dataSnapshot.getChildren()) {
                            Map<String, Object> tripData = (Map<String, Object>) tripSnap.getValue();
                            if (tripData != null) {
                                // Process from and to fields just like for customer
                                String from = (String) tripData.get("from");
                                String to = (String) tripData.get("to");
                                if (from != null) {
                                    if (from.contains(" ")) {
                                        from = from.split(" ")[0];
                                    }
                                    if (from.endsWith(",")) {
                                        from = from.substring(0, from.length() - 1);
                                    }
                                    tripData.put("from", from);
                                }
                                if (to != null) {
                                    if (to.contains(" ")) {
                                        to = to.split(" ")[0];
                                    }
                                    if (to.endsWith(",")) {
                                        to = to.substring(0, to.length() - 1);
                                    }
                                    tripData.put("to", to);
                                }
                                result.put(tripSnap.getKey(), tripData);
                            }
                        }
                        future.complete(result);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Map<String, Object>> getTripsNearLocation(double queryLat, double queryLon) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        tripsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> result = new HashMap<>();
                for (DataSnapshot tripSnap : dataSnapshot.getChildren()) {
                    Map<String, Object> tripData = (Map<String, Object>) tripSnap.getValue();
                    if (tripData == null) continue;

                    try {
                        double fromLat = Double.parseDouble(String.valueOf(tripData.get("fromLat")));
                        double fromLon = Double.parseDouble(String.valueOf(tripData.get("fromLon")));
                        String status = String.valueOf(tripData.get("status"));

                        // ✅ Only include trips with status = "Pending"
                        if (!"Pending".equalsIgnoreCase(status)) continue;

                        double distance = haversineDistance(queryLat, queryLon, fromLat, fromLon);

                        if (distance <= 20.0) {
                            result.put(tripSnap.getKey(), tripData);
                        }
                    } catch (Exception ignored) {
                        // Skip this trip if parsing fails
                    }
                }
                future.complete(result);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

}
