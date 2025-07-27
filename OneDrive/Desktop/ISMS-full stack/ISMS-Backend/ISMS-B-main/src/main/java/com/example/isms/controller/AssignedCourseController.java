package com.example.isms.controller;

import com.example.isms.model.AssignedCourse;
import com.example.isms.model.SectionInfo;
import com.example.isms.service.AssignedCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/assigned_courses")
public class AssignedCourseController {

    @Autowired
    private AssignedCourseService assignedCourseService;


    @PostMapping("/assign/bulk")
    public ResponseEntity<String> assignCoursesBulk(@RequestBody List<Map<String, Object>> coursesPayload) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map<String, Object> payload : coursesPayload) {
            String facultyName = (String) payload.get("facultyName");

            AssignedCourse course = new AssignedCourse();
            course.setCourseId((String) payload.get("courseId"));
            course.setCourseName((String) payload.get("courseName"));
            course.setSemester((String) payload.get("semester"));
            course.setRoomNumber((String) payload.get("roomNumber"));
            course.setCredits((Integer) payload.get("credits"));
            course.setFacultyName((String) payload.get("facultyName"));

            // Manual conversion of nested map
            Map<String, Map<String, String>> rawBranchSection = (Map<String, Map<String, String>>) payload.get("branchSection");
            Map<String, SectionInfo> converted = new HashMap<>();

            for (String section : rawBranchSection.keySet()) {
                Map<String, String> sectionData = rawBranchSection.get(section);
                converted.put(section, new SectionInfo(
                        sectionData.get("roomNumber"),
                        sectionData.get("batch")
                ));
            }

            course.setBranchSection(converted);

            CompletableFuture<Void> future = assignedCourseService.assignCourseToFaculty(facultyName, course);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return ResponseEntity.ok("All courses assigned with batch and room info.");
    }




    @GetMapping("/by-email/{email}")
    public CompletableFuture<ResponseEntity<List<AssignedCourse>>> getCoursesByEmail(@PathVariable String email) {
        return assignedCourseService.getCoursesByFacultyEmail(email)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


    @GetMapping("/by-course-id/{courseId}")
    public CompletableFuture<ResponseEntity<AssignedCourse>> getCourseByCourseId(@PathVariable String courseId) {
        return assignedCourseService.getAssignedCourseByCourseId(courseId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null));
    }

}

