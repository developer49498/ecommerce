package com.example.isms.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StudentSearchService {

    public List<Map<String, Object>> searchStudentsByName(
            String branch,
            String programme,
            String year,
            String name
    ) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        String lowername = name.toLowerCase();

        ApiFuture<QuerySnapshot> future = db.collection("userIndex")
                .whereGreaterThanOrEqualTo("name", lowername)
                .whereLessThanOrEqualTo("name", lowername + "\uf8ff")
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (documents.isEmpty()) {
            return resultList;
        }

        for (QueryDocumentSnapshot doc : documents) {
            String studentId = doc.getString("id");

            try {
                Map<String, Object> studentData = searchStudentById(branch, programme, year, studentId);
                resultList.add(studentData);
            } catch (Exception e) {
                System.err.println("Error fetching student by ID " + studentId + ": " + e.getMessage());
            }
        }

        return resultList;
    }



    public Map<String, Object> searchStudentById(
            String branch,
            String programme,
            String year,
            String studentId
    ) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        DocumentReference profileRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("details").document("profile");

        DocumentSnapshot profileSnapshot = profileRef.get().get();

        if (!profileSnapshot.exists()) {
            throw new NoSuchElementException("Student not found.");
        }

        Map<String, Object> studentData = new HashMap<>(profileSnapshot.getData());

        // Format lastLogin
        Object lastLoginObj = studentData.get("lastLogin");
        if (lastLoginObj instanceof Timestamp) {
            Timestamp lastLoginTimestamp = (Timestamp) lastLoginObj;
            ZonedDateTime zdt = lastLoginTimestamp.toDate().toInstant()
                    .atZone(ZoneId.of("Asia/Kolkata"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm:ss 'UTC+5:30'");
            String formattedLastLogin = zdt.format(formatter);
            studentData.put("lastLogin", formattedLastLogin);
        } else {
            studentData.put("lastLogin", "N/A");
        }

        // Fetch grade
        DocumentReference resultRef = db
                .collection("branch").document(branch)
                .collection("programme").document(programme)
                .collection("year").document(year)
                .collection("students").document(studentId)
                .collection("result").document("grade");

        DocumentSnapshot resultSnapshot = resultRef.get().get();
        String grade = "N/A";
        if (resultSnapshot.exists()) {
            Object cgpaObj = resultSnapshot.get("cgpa");
            if (cgpaObj != null) {
                grade = cgpaObj.toString();
            }
        }

        // Add fallback identifying fields
        studentData.putIfAbsent("id", studentId);
        studentData.putIfAbsent("branch", branch);
        studentData.putIfAbsent("programme", programme);
        studentData.putIfAbsent("year", year);
        studentData.put("grade", grade);

        return studentData;
    }
}
