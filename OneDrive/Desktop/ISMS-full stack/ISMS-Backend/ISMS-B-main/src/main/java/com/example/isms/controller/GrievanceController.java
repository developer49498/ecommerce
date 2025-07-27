package com.example.isms.controller;

import com.example.isms.model.Grievance;
import com.example.isms.model.GrievanceWithStatus;
import com.example.isms.service.GrievanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/grievances")
public class GrievanceController {

    private final GrievanceService grievanceService;

    public GrievanceController(GrievanceService grievanceService) {
        this.grievanceService = grievanceService;
    }

    // 🔹 Create a grievance
    @PostMapping("/create")
    public ResponseEntity<String> createGrievance(
            @RequestBody Grievance grievance,
            @RequestParam String studentId,
            @RequestParam String facultyNotationName
    ) {
        String grievanceId = grievanceService.createGrievance(grievance, studentId, facultyNotationName);
        return ResponseEntity.ok("Grievance created with ID: " + grievanceId);
    }

    // 🔹 Resolve a grievance
    @PostMapping("/resolve")
    public ResponseEntity<String> resolveGrievance(
            @RequestParam String grievanceId,
            @RequestParam String resolverNotationName
    ) {
        grievanceService.resolveGrievance(grievanceId, resolverNotationName);
        return ResponseEntity.ok("Grievance resolved.");
    }

    // 🔹 Forward a grievance
    @PostMapping("/forward")
    public ResponseEntity<String> forwardGrievance(
            @RequestParam String grievanceId,
            @RequestParam String fromNotationName,
            @RequestParam String toNotationName,
            @RequestParam String forwardingNote
    ) {
        grievanceService.forwardGrievance(grievanceId, fromNotationName, toNotationName,forwardingNote);
        return ResponseEntity.ok("Grievance forwarded to " + toNotationName);
    }


    @GetMapping("/by-email/full")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getFullGrievancesByEmail(
            @RequestParam String email) {
        return grievanceService.getFullGrievancesByEmail(email)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/by-email/status")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getGrievancesByEmailAndStatus(
            @RequestParam String email,
            @RequestParam String status) {
        return grievanceService.getGrievancesByEmailAndStatus(email, status)
                .thenApply(ResponseEntity::ok);
    }

}
