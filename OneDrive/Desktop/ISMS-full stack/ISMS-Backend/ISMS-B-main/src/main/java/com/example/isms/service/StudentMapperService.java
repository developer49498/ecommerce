package com.example.isms.service;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class StudentMapperService {

    private final Firestore firestore = FirestoreClient.getFirestore();

    @Autowired
    private AssignedCourseService assignedCourseService;

    public CompletableFuture<Void> storeStudentIds(String semester, String batch, String section, List<String> studentIds) {
        return CompletableFuture.runAsync(() -> {
            try {
                String documentId = semester + "_" + batch + "_" + section;
                for (String studentId : studentIds) {
                    firestore.collection("student_id_mapper")
                            .document(semester)
                            .collection(documentId)
                            .document("student_ids")
                            .collection("ids")
                            .document(studentId)
                            .set(Map.of("id", studentId));


                    DocumentReference indexDocRef = firestore
                            .collection("userIndex")
                            .document(studentId);

                    Map<String, Object> indexData = new HashMap<>();
                    indexData.put("section_branch", section);
                    indexDocRef.set(indexData, SetOptions.merge());


                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to store student IDs: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> storeCourseIds(String semester, String batch, String section, List<String> courseIds) {
        return CompletableFuture.runAsync(() -> {
            try {
                String documentId = semester + "_" + batch + "_" + section;
                for (String courseId : courseIds) {
                    firestore.collection("student_id_mapper")
                            .document(semester)
                            .collection(documentId)
                            .document("courses")
                            .collection("ids")
                            .document(courseId)
                            .set(Map.of("id", courseId));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to store course IDs: " + e.getMessage());
            }
        });
    }



    public CompletableFuture<List<String>> getCourseIdsByStudentEmailAndSemester(String email, String semester) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Extract student ID from email
                String studentId = email.split("@")[0];

                // Step 2: Extract batch year from 3rd and 4th characters (e.g., b123048 → "23" → "2023")
                String batchCode = studentId.substring(2, 4);  // 3rd & 4th characters
                String batchYear = "20" + batchCode;

                // Step 3: Fetch section from userIndex collection
                DocumentSnapshot indexSnapshot = firestore
                        .collection("userIndex")
                        .document(studentId)
                        .get()
                        .get(); // blocking get for async task

                if (!indexSnapshot.exists()) {
                    throw new RuntimeException("No section info found for student: " + studentId);
                }

                String section = indexSnapshot.getString("section_branch");
                if (section == null) {
                    throw new RuntimeException("Section is null for student: " + studentId);
                }

                // Step 4: Construct the document ID
                String documentId = semester + "_" + batchYear + "_" + section;

                // Step 5: Fetch all course IDs under: student_id_mapper/{semester}/{documentId}/courses/ids/
                CollectionReference coursesRef = firestore
                        .collection("student_id_mapper")
                        .document(semester)
                        .collection(documentId)
                        .document("courses")
                        .collection("ids");

                ApiFuture<QuerySnapshot> future = coursesRef.get();
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                List<String> courseIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : documents) {
                    courseIds.add(doc.getId()); // Or doc.getString("id") if stored as a field
                }

                return courseIds;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch course IDs: " + e.getMessage(), e);
            }
        });
    }


    public CompletableFuture<List<String>> getStudentIdsBySectionAndBatch(String semester, String batch, String section) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String documentId = semester + "_" + batch + "_" + section;

                CollectionReference studentIdsRef = firestore
                        .collection("student_id_mapper")
                        .document(semester)
                        .collection(documentId)
                        .document("student_ids")
                        .collection("ids");

                ApiFuture<QuerySnapshot> future = studentIdsRef.get();
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                List<String> studentIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : documents) {
                    // Each document ID is studentId or you can also get from "id" field
                    studentIds.add(doc.getId());
                }
                return studentIds;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch student IDs: " + e.getMessage(), e);
            }
        });
    }


    public CompletableFuture<List<Map<String, String>>> getStudentIdAndNames(
            String semester, String batch, String section) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String documentId = semester + "_" + batch + "_" + section;

                CollectionReference studentIdsRef = firestore
                        .collection("student_id_mapper")
                        .document(semester)
                        .collection(documentId)
                        .document("student_ids")
                        .collection("ids");

                List<QueryDocumentSnapshot> studentDocs = studentIdsRef.get().get().getDocuments();

                List<Map<String, String>> studentList = new ArrayList<>();

                for (QueryDocumentSnapshot doc : studentDocs) {
                    String studentId = doc.getId();

                    DocumentSnapshot indexSnapshot = firestore
                            .collection("userIndex")
                            .document(studentId)
                            .get()
                            .get();

                    if (indexSnapshot.exists()) {
                        String name = indexSnapshot.getString("name");

                        Map<String, String> studentData = new HashMap<>();
                        studentData.put("studentId", studentId);
                        studentData.put("name", name != null ? name : "Unknown");

                        studentList.add(studentData);
                    }
                }

                return studentList;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch student names: " + e.getMessage(), e);
            }
        });
    }




    public CompletableFuture<List<Map<String, String>>> getStudentCourseMetadata(String email, String semester) {
        return getCourseIdsByStudentEmailAndSemester(email, semester).thenCompose(courseIds -> {
            List<CompletableFuture<Map<String, String>>> futures = new ArrayList<>();

            for (String courseId : courseIds) {
                CompletableFuture<Map<String, String>> future = assignedCourseService.getAssignedCourseByCourseId(courseId)
                        .thenApply(course -> {
                            Map<String, String> courseInfo = new HashMap<>();
                            courseInfo.put("courseId", course.getCourseId());
                            courseInfo.put("courseName", course.getCourseName());
                            courseInfo.put("facultyName", course.getFacultyName());
                            courseInfo.put("credits", String.valueOf(course.getCredits()));
                            return courseInfo;
                        });
                futures.add(future);
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> {
                        List<Map<String, String>> result = new ArrayList<>();
                        for (CompletableFuture<Map<String, String>> f : futures) {
                            result.add(f.join());  // Safe after allOf
                        }
                        return result;
                    });
        });
    }





}
