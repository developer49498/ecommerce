package com.example.isms.service;

import com.example.isms.model.AssignedCourse;
import com.example.isms.model.SectionInfo;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AssignedCourseService {

    @Autowired
    private Firestore firestore;

    public CompletableFuture<Void> assignCourseToFaculty(String facultyName, AssignedCourse course) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Step 1: Get faculty email from userIndexFaculty
                ApiFuture<QuerySnapshot> snapshot = firestore.collection("userIndexFaculty")
                        .whereEqualTo("name", facultyName.toLowerCase())
                        .get();


                List<QueryDocumentSnapshot> documents = snapshot.get().getDocuments();
                if (documents.isEmpty()) throw new RuntimeException("Faculty not found");

                String email = documents.get(0).getString("email");

                // Step 4: Save reverse index for courseId -> faculty email
                DocumentReference reverseIndexRef = firestore.collection("courseIndexFaculty")
                        .document(course.getCourseId());

                Map<String, Object> reverseIndexData = Map.of("email", email);

                reverseIndexRef.set(reverseIndexData);

                // Step 2: Save metadata
                DocumentReference metadataRef = firestore.collection("assigned_courses")
                        .document(email)
                        .collection(course.getCourseId())
                        .document("metadata");

                metadataRef.set(course);

                // Step 3: Save branch_section rooms
                DocumentReference branchSectionRef = firestore.collection("assigned_courses")
                        .document(email)
                        .collection(course.getCourseId())
                        .document("branch_section");

                branchSectionRef.set(course.getBranchSection());

            } catch (Exception e) {
                throw new RuntimeException("Failed to assign course: " + e.getMessage());
            }
        });
    }



    public CompletableFuture<List<AssignedCourse>> getCoursesByFacultyEmail(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<AssignedCourse> assignedCourses = new java.util.ArrayList<>();

                // Get list of subcollections under the faculty document
                Iterable<CollectionReference> courseCollections = firestore.collection("assigned_courses")
                        .document(email)
                        .listCollections(); // ✅ NO `.get()` here


                for (CollectionReference courseCollection : courseCollections) {
                    String courseId = courseCollection.getId();

                    // fetch metadata
                    AssignedCourse course = courseCollection.document("metadata").get().get().toObject(AssignedCourse.class);

                    // fetch branch_section
                    Map<String, Object> branchSectionMap = courseCollection.document("branch_section").get().get().getData();

                    if (course != null && branchSectionMap != null) {
                        Map<String, SectionInfo> parsedMap = new java.util.HashMap<>();
                        for (String key : branchSectionMap.keySet()) {
                            Object val = branchSectionMap.get(key);
                            if (val instanceof Map) {
                                Map<?, ?> sectionData = (Map<?, ?>) val;
                                String roomNumber = (String) sectionData.get("roomNumber");
                                String batch = (String) sectionData.get("batch");
                                parsedMap.put(key, new SectionInfo(roomNumber, batch));
                            }
                        }
                        course.setBranchSection(parsedMap);
                        assignedCourses.add(course);
                    }
                }


                return assignedCourses;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch assigned courses: " + e.getMessage(), e);
            }
        });
    }


    public CompletableFuture<AssignedCourse> getAssignedCourseByCourseId(String courseId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Lookup faculty email from reverse index
                DocumentSnapshot reverseIndexDoc = firestore.collection("courseIndexFaculty")
                        .document(courseId)
                        .get()
                        .get();

                if (!reverseIndexDoc.exists()) {
                    throw new RuntimeException("No faculty assigned to courseId: " + courseId);
                }

                String facultyEmail = reverseIndexDoc.getString("email");

                // Step 2: Get metadata
                DocumentSnapshot metadataDoc = firestore.collection("assigned_courses")
                        .document(facultyEmail)
                        .collection(courseId)
                        .document("metadata")
                        .get()
                        .get();

                AssignedCourse course = metadataDoc.toObject(AssignedCourse.class);

                if (course == null) {
                    throw new RuntimeException("Metadata not found for courseId: " + courseId);
                }

                // Step 3: Get branch_section
                DocumentSnapshot branchSectionDoc = firestore.collection("assigned_courses")
                        .document(facultyEmail)
                        .collection(courseId)
                        .document("branch_section")
                        .get()
                        .get();

                Map<String, Object> branchSectionMap = branchSectionDoc.getData();
                if (branchSectionMap != null) {
                    Map<String, SectionInfo> parsedMap = new java.util.HashMap<>();
                    for (String key : branchSectionMap.keySet()) {
                        Object val = branchSectionMap.get(key);
                        if (val instanceof Map) {
                            Map<?, ?> sectionData = (Map<?, ?>) val;
                            String roomNumber = (String) sectionData.get("roomNumber");
                            String batch = (String) sectionData.get("batch");
                            parsedMap.put(key, new SectionInfo(roomNumber, batch));
                        }
                    }
                    course.setBranchSection(parsedMap);
                }

                return course;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch course by ID: " + e.getMessage(), e);
            }
        });
    }

}

