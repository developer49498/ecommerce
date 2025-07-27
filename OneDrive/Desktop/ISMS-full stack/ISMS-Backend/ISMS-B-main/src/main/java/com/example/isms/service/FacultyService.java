package com.example.isms.service;

import com.example.isms.model.Faculty;
import com.example.isms.model.TeachingAssignment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class FacultyService {

    private final Firestore db = FirestoreClient.getFirestore();

    public String saveBulkFaculty(List<Faculty> facultyList) throws ExecutionException, InterruptedException {
        for (Faculty faculty : facultyList) {
            String facultyId = faculty.getId();

            // Reference to the profile document
            DocumentReference profileRef = db
                    .collection("department")
                    .document(faculty.getDepartment().toUpperCase())
                    .collection("faculty")
                    .document(facultyId)
                    .collection("details")
                    .document("profile");




            DocumentReference gmapRef = db
                    .collection("grievance_map")
                    .document(faculty.getContact().toLowerCase());
            Map<String, Object> gmapData = new HashMap<>();

            if (faculty.getAdministrative_position() != null && !faculty.getAdministrative_position().isEmpty()) {
                gmapData.put("notation_name", faculty.getAdministrative_position());
            } else {
                gmapData.put("notation_name", faculty.getName());
            }

// Save the gmap data
            gmapRef.set(gmapData).get();





            // Prepare profile data without currentlyTeaching
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", faculty.getId());
            profileData.put("name", faculty.getName());
            profileData.put("contact", faculty.getContact());
            profileData.put("department", faculty.getDepartment().toUpperCase());
            profileData.put("designation", faculty.getDesignation());
            profileData.put("experience", faculty.getExperience());
            profileData.put("qualification", faculty.getQualification());
            profileData.put("administrative_position",faculty.getAdministrative_position());

            // Save profile data
            profileRef.set(profileData).get();




            // Save currentlyTeaching as a subcollection under profileRef
            List<TeachingAssignment> teachingList = faculty.getCurrentlyTeaching();
            if (teachingList != null) {
                for (TeachingAssignment assignment : teachingList) {
                    String className = assignment.getClassId();
                    String subject = assignment.getSubject();

                    DocumentReference teachingDocRef = profileRef
                            .collection("currentlyTeaching")
                            .document(className);

                    Map<String, Object> subjectData = Map.of("subject", subject);

                    teachingDocRef.set(subjectData);
                }
            }

            // userIndex for search by name
            Map<String, Object> indexData = new HashMap<>();
            indexData.put("id", faculty.getId());
            indexData.put("name", faculty.getName().toLowerCase());
            indexData.put("department", faculty.getDepartment().toUpperCase());
            indexData.put("email", faculty.getContact());

// Only add administrative_position if it's not null or empty
            if (faculty.getAdministrative_position() != null && !faculty.getAdministrative_position().isEmpty()) {
                indexData.put("administrative_position", faculty.getAdministrative_position().toUpperCase());
            }

            db.collection("userIndexFaculty")
                    .document(facultyId)
                    .set(indexData)
                    .get();

        }
        return null;
    }



    public Faculty getFacultyById(String facultyId) throws ExecutionException, InterruptedException {
        // Step 1: Retrieve department from index
        DocumentReference indexRef = db.collection("userIndexFaculty").document(facultyId.toUpperCase());
        DocumentSnapshot indexSnapshot = indexRef.get().get();

        if (!indexSnapshot.exists()) return null;

        String department = indexSnapshot.getString("department");
        if (department == null || department.isEmpty()) return null;

        // Step 2: Retrieve profile document
        DocumentReference profileRef = db
                .collection("department")
                .document(department.toUpperCase())
                .collection("faculty")
                .document(facultyId.toUpperCase())
                .collection("details")
                .document("profile");

        DocumentSnapshot profileSnapshot = profileRef.get().get();
        if (!profileSnapshot.exists()) return null;

        Map<String, Object> data = profileSnapshot.getData();
        if (data == null) return null;

        Faculty faculty = new Faculty();

        // Basic fields
        faculty.setId(facultyId.toUpperCase());
        faculty.setDepartment(department);
        faculty.setName((String) data.getOrDefault("name", ""));
        faculty.setExperience((String) data.getOrDefault("experience", ""));
        faculty.setQualification((String) data.getOrDefault("qualification", ""));
        faculty.setContact((String) data.getOrDefault("contact", ""));
        faculty.setDesignation((String) data.getOrDefault("designation", ""));
        faculty.setAdministrative_position((String) data.getOrDefault("administrative_position", ""));

        // lastLogin formatting
        Object lastLoginObj = data.get("lastLogin");
        if (lastLoginObj instanceof com.google.cloud.Timestamp) {
            com.google.cloud.Timestamp lastLoginTimestamp = (com.google.cloud.Timestamp) lastLoginObj;
            ZonedDateTime zdt = lastLoginTimestamp.toDate().toInstant()
                    .atZone(ZoneId.of("Asia/Kolkata")); // UTC+5:30
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm:ss 'UTC+5:30'");
            String formattedLastLogin = zdt.format(formatter);
            faculty.setLastLogin(formattedLastLogin);
        } else {
            faculty.setLastLogin("N/A");
        }

        // currentlyTeaching subcollection
        ApiFuture<QuerySnapshot> currentlyTeachingFuture = profileRef
                .collection("currentlyTeaching")
                .get();

        List<TeachingAssignment> teachingList = new ArrayList<>();
        for (QueryDocumentSnapshot doc : currentlyTeachingFuture.get().getDocuments()) {
            TeachingAssignment assignment = new TeachingAssignment();
            assignment.setClassId(doc.getId());
            assignment.setSubject(doc.getString("subject"));
            teachingList.add(assignment);
        }
        faculty.setCurrentlyTeaching(teachingList);

        return faculty;
    }



    public List<Faculty> searchFacultyByName(String name) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("userIndexFaculty")
                .whereGreaterThanOrEqualTo("name", name.toLowerCase())
                .whereLessThanOrEqualTo("name", name.toLowerCase() + "\uf8ff")
                .get();

        List<Faculty> results = new ArrayList<>();
        for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
            String facultyId = doc.getId();
            Faculty faculty = getFacultyById(facultyId);
            if (faculty != null) results.add(faculty);
        }
        return results;
    }
    public List<Faculty> getFacultyByDepartment(String department) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("userIndexFaculty")
                .whereEqualTo("department", department.toUpperCase())
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        List<CompletableFuture<Faculty>> futures = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs) {
            String facultyId = doc.getId();
            CompletableFuture<Faculty> facultyFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getFacultyById(facultyId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
            futures.add(facultyFuture);
        }

        List<Faculty> results = new ArrayList<>();
        for (CompletableFuture<Faculty> futureFaculty : futures) {
            Faculty faculty = futureFaculty.get();
            if (faculty != null) results.add(faculty);
        }

        return results;
    }



    public Faculty getFacultyByEmail(String email) throws ExecutionException, InterruptedException {
        // Query userIndexFaculty for the matching email
        ApiFuture<QuerySnapshot> future = db.collection("userIndexFaculty")
                .whereEqualTo("email", email)
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        if (docs.isEmpty()) return null;

        // Use the first match (assuming email is unique)
        String facultyId = docs.get(0).getId();
        return getFacultyById(facultyId);
    }


    public List<Faculty> getFacultyByAdministrativePosition(String position) throws ExecutionException, InterruptedException {
        // Query userIndexFaculty by administrative_position (stored in uppercase)
        ApiFuture<QuerySnapshot> future = db.collection("userIndexFaculty")
                .whereEqualTo("administrative_position", position.toUpperCase())
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        List<CompletableFuture<Faculty>> futures = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs) {
            String facultyId = doc.getId();
            CompletableFuture<Faculty> facultyFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getFacultyById(facultyId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
            futures.add(facultyFuture);
        }

        List<Faculty> results = new ArrayList<>();
        for (CompletableFuture<Faculty> futureFaculty : futures) {
            Faculty faculty = futureFaculty.get();
            if (faculty != null) results.add(faculty);
        }

        return results;
    }


    public List<Faculty> getAllFaculty() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("userIndexFaculty").get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();

        List<CompletableFuture<Faculty>> futures = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs) {
            String facultyId = doc.getId();
            CompletableFuture<Faculty> facultyFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getFacultyById(facultyId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
            futures.add(facultyFuture);
        }

        List<Faculty> allFaculty = new ArrayList<>();
        for (CompletableFuture<Faculty> futureFaculty : futures) {
            Faculty faculty = futureFaculty.get();
            if (faculty != null) allFaculty.add(faculty);
        }

        return allFaculty;
    }

}
