package com.example.isms.controller;

import com.example.isms.model.CourseResult;
import com.example.isms.model.SemesterResult;
import com.example.isms.model.Student;
import com.example.isms.service.ResultService;
import com.example.isms.service.StudentSearchService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.google.cloud.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {


    @Autowired
    private ResultService resultService;


    @Autowired
    private StudentSearchService studentSearchService;

    // Save students
    @PostMapping("/save_students")
    public ResponseEntity<String> saveStudents(@RequestBody List<Student> students) throws Exception {
        Firestore db = FirestoreClient.getFirestore();


        for (Student student : students) {


            String debugPath = String.format(
                    "branch/%s/programme/%s/year/%s/students/%s/details/profile",
                    student.getBranch(),
                    student.getProgramme(),
                    student.getEnrollmentYear(),
                    student.getId()
            );
            System.out.println("DEBUG PATH = " + debugPath);


            DocumentReference docRef = db
                    .collection("branch")
                    .document(student.getBranch())
                    .collection("programme")
                    .document(student.getProgramme())
                    .collection("year")
                    .document(student.getEnrollmentYear())
                    .collection("students")
                    .document(student.getId())
                    .collection("details")
                    .document("profile");

            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("id", student.getId());
            studentMap.put("name", student.getName());
            studentMap.put("branch", student.getBranch());
            studentMap.put("programme", student.getProgramme());
            studentMap.put("email", student.getEmail());
            studentMap.put("phone", student.getPhone());

            docRef.set(studentMap);


            DocumentReference emailRef = db
                    .collection("email-name")
                    .document(student.getEmail());
            Map<String ,Object>emaildata=new HashMap<>();
            emaildata.put("email",student.getEmail());
            emaildata.put("name",student.getName());

            emailRef.set(emaildata);

            // New userIndex collection save (flat)
            DocumentReference indexDocRef = db
                    .collection("userIndex")
                    .document(student.getId());

            Map<String, Object> indexData = new HashMap<>();
            indexData.put("name", student.getName().toLowerCase());
            indexData.put("id", student.getId());

            indexDocRef.set(indexData);
        }

        return ResponseEntity.ok("Students saved successfully.");
    }

    // Bulk Save results for all students in a branch/year
    // Bulk Save results for all students in a branch/year
    @PostMapping("/results/bulk/{branch}/{programme}/{year}")
    public ResponseEntity<String> addResultsForAllStudents(
            @PathVariable String branch,
            @PathVariable String programme,
            @PathVariable String year,
            @RequestBody Map<String, Map<String, SemesterResult>> studentResults) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        for (Map.Entry<String, Map<String, SemesterResult>> studentEntry : studentResults.entrySet()) {
            String studentId = studentEntry.getKey();
            Map<String, SemesterResult> semesters = studentEntry.getValue();

            for (Map.Entry<String, SemesterResult> semEntry : semesters.entrySet()) {
                String semester = semEntry.getKey();
                SemesterResult semResult = semEntry.getValue();

                resultService.unpublishResult(semester, branch, programme, year);

                // Store each course result
                if (semResult.getCourses() != null) {
                    for (Map.Entry<String, CourseResult> courseEntry : semResult.getCourses().entrySet()) {
                        String courseId = courseEntry.getKey();
                        CourseResult course = courseEntry.getValue();

                        db.collection("branch")
                                .document(branch)
                                .collection("programme")
                                .document(programme)
                                .collection("year")
                                .document(year)
                                .collection("students")
                                .document(studentId)
                                .collection("result")
                                .document(semester)
                                .collection("courses")
                                .document(courseId)
                                .set(course);
                    }
                }

                // Store final semester result
                if (semResult.getFinalResult() != null) {


                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("sgpa", semResult.getFinalResult().getSGPA());


                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("cgpa", semResult.getFinalResult().getCGPA());


                    db.collection("branch")
                            .document(branch)
                            .collection("programme")
                            .document(programme)
                            .collection("year")
                            .document(year)
                            .collection("students")
                            .document(studentId)
                            .collection("result")
                            .document(semester)
                            .collection("final_result")
                            .document("summary")
                            .set(map1);



                    db.collection("branch")
                            .document(branch)
                            .collection("programme")
                            .document(programme)
                            .collection("year")
                            .document(year)
                            .collection("students")
                            .document(studentId)
                            .collection("result")
                            .document("grade")
                            .set(map2);
                }
            }
        }

        return ResponseEntity.ok("Results saved for all students.");
    }


    // Save results per student (individual)
    @PostMapping("/results/{branch}/{year}/{studentId}")
    public ResponseEntity<String> addStudentResults(
            @PathVariable String branch,
            @PathVariable String year,
            @PathVariable String studentId,
            @RequestBody Map<String, SemesterResult> results) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        for (Map.Entry<String, SemesterResult> semEntry : results.entrySet()) {
            String semester = semEntry.getKey();
            SemesterResult semResult = semEntry.getValue();

            // Store each course in semester
            for (Map.Entry<String, CourseResult> courseEntry : semResult.getCourses().entrySet()) {
                String courseId = courseEntry.getKey();
                CourseResult course = courseEntry.getValue();

                db.collection("branch")
                        .document(branch)
                        .collection(year)
                        .document(studentId)
                        .collection("result")
                        .document(semester)
                        .collection("courses")
                        .document(courseId)
                        .set(course);
            }

            // Store final result
            db.collection("branch")
                    .document(branch)
                    .collection(year)
                    .document(studentId)
                    .collection("result")
                    .document(semester)
                    .collection("final_result")
                    .document("summary")
                    .set(semResult.getFinalResult().getSGPA());





            db.collection("branch")
                    .document(branch)
                    .collection(year)
                    .document(studentId)
                    .collection("result")
                    .document("cgpa")
                    .set(semResult.getFinalResult().getCGPA());

        }

        return ResponseEntity.ok("Results saved successfully.");
    }





    @GetMapping("/searchById/{branch}/{programme}/{year}/{studentId}")
    public ResponseEntity<?> searchStudentById(
            @PathVariable String branch,
            @PathVariable String programme,
            @PathVariable String year,
            @PathVariable String studentId
    ) {
        try {
            Map<String, Object> studentData = studentSearchService.searchStudentById(branch, programme, year, studentId);
            return ResponseEntity.ok(studentData);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }




    @GetMapping("/searchByNameRecursive/{branch}/{programme}/{year}")
    public ResponseEntity<?> searchStudentsByNameRecursive(
            @PathVariable String branch,
            @PathVariable String programme,
            @PathVariable String year,
            @RequestParam String name
    ) {
        try {
            List<Map<String, Object>> resultList = studentSearchService.searchStudentsByName(branch, programme, year, name);
            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/results/{branch}/{programme}/{year}/{studentId}/semester/{semester}")
    public ResponseEntity<?> getSemesterResult(
            @PathVariable String branch,
            @PathVariable String programme,
            @PathVariable String year,
            @PathVariable String studentId,
            @PathVariable String semester
    ) throws Exception {

        // Check if result is published
        boolean isPublished = resultService.isResultPublished(semester, branch, programme, year);
        if (!isPublished) {
            return ResponseEntity.ok(Collections.emptyList()); // Return empty array: []
        }

        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> resultData = new HashMap<>();

        // Fetch course details
        CollectionReference courseRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document(semester)
                .collection("courses");

        ApiFuture<QuerySnapshot> courseFuture = courseRef.get();
        List<QueryDocumentSnapshot> courseDocs = courseFuture.get().getDocuments();
        List<Map<String, Object>> courseList = new ArrayList<>();

        for (DocumentSnapshot doc : courseDocs) {
            Map<String, Object> courseData = new HashMap<>(doc.getData());
            courseData.put("courseId", doc.getId()); // Add courseId
            courseList.add(courseData);
        }

        resultData.put("courses", courseList);

        // Fetch SGPA
        DocumentReference sgpaRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document(semester)
                .collection("final_result").document("summary");

        DocumentSnapshot sgpaSnap = sgpaRef.get().get();
        resultData.put("sgpa", sgpaSnap.exists() ? sgpaSnap.get("sgpa") : "N/A");

        // Fetch CGPA
        DocumentReference cgpaRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document("grade");

        DocumentSnapshot cgpaSnap = cgpaRef.get().get();
        resultData.put("cgpa", cgpaSnap.exists() ? cgpaSnap.get("cgpa") : "N/A");

        return ResponseEntity.ok(resultData);
    }

    @GetMapping("/results/{branch}/{programme}/{year}/{studentId}/all-semesters")
    public ResponseEntity<?> getAllSemesterResults(
            @PathVariable String branch,
            @PathVariable String programme,
            @PathVariable String year,
            @PathVariable String studentId
    ) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> allCourses = new ArrayList<>();

        // Call your existing method or service to get published semesters
        List<String> publishedSemesters = resultService.getPublishedSemesters(branch, year, programme);

        for (String semester : publishedSemesters) {
            // Instead of duplicating code, you can reuse your getSemesterResult logic
            // But since getSemesterResult returns ResponseEntity, better to extract the fetching logic to a separate private method

            Map<String, Object> semesterResult = fetchSemesterResult(branch, programme, year, studentId, semester);
            if (semesterResult != null && semesterResult.containsKey("courses")) {
                List<Map<String, Object>> courses = (List<Map<String, Object>>) semesterResult.get("courses");
                allCourses.addAll(courses);
            }
        }

        // Fetch CGPA once (similar to your existing code)
        DocumentReference cgpaRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document("grade");

        DocumentSnapshot cgpaSnap = cgpaRef.get().get();
        response.put("cgpa", cgpaSnap.exists() ? cgpaSnap.getDouble("cgpa") : "N/A");
        response.put("courses", allCourses);

        return ResponseEntity.ok(response);
    }

    // Extracted helper method to fetch semester result data similar to your first method but returning Map
    private Map<String, Object> fetchSemesterResult(String branch, String programme, String year, String studentId, String semester) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // Check if result is published (optional here because you already filter on published semesters)
        boolean isPublished = resultService.isResultPublished(semester, branch, programme, year);
        if (!isPublished) {
            return null; // or empty map
        }

        Map<String, Object> resultData = new HashMap<>();

        CollectionReference courseRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document(semester)
                .collection("courses");

        ApiFuture<QuerySnapshot> courseFuture = courseRef.get();
        List<QueryDocumentSnapshot> courseDocs = courseFuture.get().getDocuments();
        List<Map<String, Object>> courseList = new ArrayList<>();

        for (DocumentSnapshot doc : courseDocs) {
            Map<String, Object> courseData = new HashMap<>(doc.getData());
            courseData.put("courseId", doc.getId());
            courseList.add(courseData);
        }

        resultData.put("courses", courseList);

        // Fetch SGPA - optional here because you want flat list, but you can include if needed
        DocumentReference sgpaRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document(semester)
                .collection("final_result").document("summary");

        DocumentSnapshot sgpaSnap = sgpaRef.get().get();
        resultData.put("sgpa", sgpaSnap.exists() ? sgpaSnap.get("sgpa") : "N/A");

        return resultData;
    }


}







