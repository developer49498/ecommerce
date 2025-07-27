package com.example.isms.controller;

import com.example.isms.service.StudentFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student-fee")
public class StudentFeeController {

    @Autowired
    private StudentFeeService studentFeeService;

    @PostMapping("/store")
    public ResponseEntity<String> storeFeeForFilteredStudents(
            @RequestParam String program,
            @RequestParam String branch,
            @RequestParam String hostel,
            @RequestParam int semester,
            @RequestBody Map<String, Object> feeData) {

        try {
            studentFeeService.storeFeeForFilteredStudents(program, branch, hostel, semester, feeData);
            return ResponseEntity.ok("Fee stored successfully for eligible students.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error storing fees: " + e.getMessage());
        }
    }

    @PostMapping("/update-outstanding")
    public ResponseEntity<String> updateOutstandingAmount(
            @RequestParam String studentId,
            @RequestParam long newOutstanding) {

        try {
            studentFeeService.updateOutstandingAmount(studentId, newOutstanding);
            return ResponseEntity.ok("Outstanding amount updated for student: " + studentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating outstanding: " + e.getMessage());
        }
    }

    @Autowired
    public StudentFeeController(StudentFeeService studentFeeService) {
        this.studentFeeService = studentFeeService;
    }

    // Endpoint 1: Get fee data for a student and semester
    @GetMapping("/fee-data")
    public Map<String, Object> getFeeData(
            @RequestParam String studentId,
            @RequestParam int semester
    ) throws Exception {
        return studentFeeService.getFeeDataForStudentAsMap(studentId, semester);
    }

    // Endpoint 2: Get outstanding amount for a student
    @GetMapping("/outstanding")
    public Map<String, ?> getOutstandingAmount(@RequestParam String studentId) throws Exception {
        Long outstanding = studentFeeService.getOutstandingAmountForStudent(studentId);
        return Map.of("studentId", studentId, "outstandingAmount", outstanding);
    }

    @PostMapping("/update-outstanding-bulk")
    public ResponseEntity<?> updateOutstandingBulk(@RequestBody List<Map<String, Object>> updates) {
        try {
            studentFeeService.bulkUpdateOutstandingAmounts(updates);
            return ResponseEntity.ok("Outstanding amounts updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating outstanding amounts: " + e.getMessage());
        }
    }
}
