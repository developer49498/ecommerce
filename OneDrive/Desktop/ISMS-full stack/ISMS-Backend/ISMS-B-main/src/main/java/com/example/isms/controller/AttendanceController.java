package com.example.isms.controller;

import com.example.isms.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @Autowired
    AttendanceService attendanceService;

    @PostMapping("/student-wise-record")
    public CompletableFuture<ResponseEntity<String>> recordStudentWiseAttendance(
            @RequestParam String courseId,
            @RequestParam String date,
            @RequestBody Map<String, Boolean> attendanceMap) {

        AttendanceService attendanceService=new AttendanceService();
        return attendanceService
                .recordStudentWiseAttendance(courseId, date, attendanceMap)
                .thenApply(v -> ResponseEntity.ok("Attendance recorded successfully"))
                .exceptionally(e -> ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
    }

    @GetMapping("/attendance-summary")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getAttendanceSummary(
            @RequestParam String courseId,
            @RequestParam String semester,
            @RequestParam String batch,
            @RequestParam String section) {
        AttendanceService attendanceService=new AttendanceService();
        return attendanceService.getAttendancePercentageByCourseAndBatch(courseId, semester, batch, section)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }



    @GetMapping("/attendance/student")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getStudentAttendanceHistory(
            @RequestParam String studentId,
            @RequestParam String courseId) {
        AttendanceService attendanceService=new AttendanceService();
        return attendanceService.getAttendanceByStudentAndCourse(studentId, courseId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/attendance/by-date")
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> getAttendanceByDate(
            @RequestParam String courseId,
            @RequestParam String semester,
            @RequestParam String batch,
            @RequestParam String section,
            @RequestParam String date) {
        AttendanceService attendanceService=new AttendanceService();
        return attendanceService.getAttendanceByCourseDateAndBatch(courseId, semester, batch, section, date)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError().build());
    }




    @GetMapping("/attendance/summary")
    public CompletableFuture<Map<String, Object>> getAttendanceSummary(
            @RequestParam String studentId,
            @RequestParam String courseId) {
        return attendanceService.getAttendanceStatsByStudentAndCourse(studentId, courseId);
    }

    @GetMapping("/attendance/status")
    public CompletableFuture<Map<String, Object>> getAttendanceStatusByDate(
            @RequestParam String studentId,
            @RequestParam String courseId,
            @RequestParam String date) {
        return attendanceService.getAttendanceByStudentAndDate(studentId, courseId, date);
    }


}
