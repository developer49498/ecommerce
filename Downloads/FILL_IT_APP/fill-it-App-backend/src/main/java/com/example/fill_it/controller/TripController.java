package com.example.fill_it.controller;

import com.example.fill_it.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<String>> createTrip(@RequestBody Map<String, String> request) {
        try {
            String customerEmail = request.get("customerEmail");
            String from = request.get("from");
            String to = request.get("to");
            String date = request.get("date");

            String fromLatStr = request.get("fromLat");
            String fromLonStr = request.get("fromLon");
            String toLatStr = request.get("toLat");
            String toLonStr = request.get("toLon");

            // ✅ Validate that lat/lon fields are not null
            if (fromLatStr == null || fromLonStr == null || toLatStr == null || toLonStr == null) {
                return CompletableFuture.completedFuture(ResponseEntity
                        .badRequest()
                        .body("Latitude or longitude values are missing"));
            }

            double fromLat = Double.parseDouble(fromLatStr.trim());
            double fromLon = Double.parseDouble(fromLonStr.trim());
            double toLat = Double.parseDouble(toLatStr.trim());
            double toLon = Double.parseDouble(toLonStr.trim());

            return tripService.createTrip(customerEmail, from, to, fromLat, fromLon, toLat, toLon, date)
                    .thenApply(tripId -> ResponseEntity.ok("Trip created with ID: " + tripId))
                    .exceptionally(ex -> ResponseEntity.internalServerError().body("Failed: " + ex.getMessage()));
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().body("Error: " + ex.getMessage()));
        }
    }


    // ✅ Accept a trip
    @PostMapping("/accept")
    public CompletableFuture<ResponseEntity<String>> acceptTrip(@RequestBody Map<String, String> request) {
        String tripId = request.get("tripId");
        String driverEmail = request.get("driverEmail");

        return tripService.acceptTrip(tripId, driverEmail)
                .thenApply(msg -> ResponseEntity.ok("Trip accepted successfully"))
                .exceptionally(ex -> ResponseEntity.internalServerError().body("Failed: " + ex.getMessage()));
    }

    // ✅ Complete a trip
    @PostMapping("/complete")
    public CompletableFuture<ResponseEntity<String>> completeTrip(@RequestBody Map<String, String> request) {
        String tripId = request.get("tripId");

        return tripService.completeTrip(tripId)
                .thenApply(msg -> ResponseEntity.ok("Trip marked as completed"))
                .exceptionally(ex -> ResponseEntity.internalServerError().body("Failed: " + ex.getMessage()));
    }

    @GetMapping("/by-customer")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getTripsByCustomerEmail(
            @RequestParam("email") String customerEmail) {

        return tripService.getTripsByCustomerEmail(customerEmail)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError()
                        .body(Map.of("error", e.getMessage())));
    }

    @GetMapping("/by-driver")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getTripsByDriverEmail(
            @RequestParam("email") String driverEmail) {

        return tripService.getTripsByDriverEmail(driverEmail)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError()
                        .body(Map.of("error", e.getMessage())));
    }
    @GetMapping("/nearby")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getNearbyTrips(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        return tripService.getTripsNearLocation(lat, lon)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage())));
    }



}
