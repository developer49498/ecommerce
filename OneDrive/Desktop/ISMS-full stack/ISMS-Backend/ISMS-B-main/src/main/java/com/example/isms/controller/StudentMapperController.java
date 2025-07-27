package com.example.isms.controller;

import com.example.isms.service.StudentMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/student-mapper")
public class StudentMapperController {

    @Autowired
    private StudentMapperService studentMapperService;

    // Endpoint 1: Store student IDs
    @PostMapping("/store-students")
    public CompletableFuture<ResponseEntity<String>> storeStudentIds(
            @RequestParam String semester,
            @RequestParam String batch,
            @RequestParam String section,
            @RequestBody List<String> studentIds) {
        return studentMapperService.storeStudentIds(semester, batch, section, studentIds)
                .thenApply(v -> ResponseEntity.ok("Student IDs stored successfully"))
                .exceptionally(e -> ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
    }

    // Endpoint 2: Store course IDs
    @PostMapping("/store-courses")
    public CompletableFuture<ResponseEntity<String>> storeCourseIds(
            @RequestParam String semester,
            @RequestParam String batch,
            @RequestParam String section,
            @RequestBody List<String> courseIds) {
        return studentMapperService.storeCourseIds(semester, batch, section, courseIds)
                .thenApply(v -> ResponseEntity.ok("Course IDs stored successfully"))
                .exceptionally(e -> ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
    }

    // Endpoint 3: Get course IDs for a student based on email and semester
    @GetMapping("/courses")
    public CompletableFuture<ResponseEntity<List<String>>> getCourseIds(
            @RequestParam String email,
            @RequestParam String semester) {
        return studentMapperService.getCourseIdsByStudentEmailAndSemester(email, semester)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }


    // Inside your StudentMapperController class

    // Endpoint 4: Get all student IDs for a given semester, batch, and section
    @GetMapping("/students")
    public CompletableFuture<ResponseEntity<List<String>>> getStudentIds(
            @RequestParam String semester,
            @RequestParam String batch,
            @RequestParam String section) {
        return studentMapperService.getStudentIdsBySectionAndBatch(semester, batch, section)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }
    @GetMapping("/students-with-names")
    public CompletableFuture<ResponseEntity<List<Map<String, String>>>> getStudentIdAndNames(
            @RequestParam String semester,
            @RequestParam String batch,
            @RequestParam String section) {

        return studentMapperService.getStudentIdAndNames(semester, batch, section)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/courses/details")
    public CompletableFuture<ResponseEntity<List<Map<String, String>>>> getCourseDetailsByStudentEmail(
            @RequestParam String email,
            @RequestParam String semester) {
        return studentMapperService.getStudentCourseMetadata(email, semester)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> {
                    e.printStackTrace(); // Log the full stack trace
                    return ResponseEntity.internalServerError().build();
                });
    }


}

