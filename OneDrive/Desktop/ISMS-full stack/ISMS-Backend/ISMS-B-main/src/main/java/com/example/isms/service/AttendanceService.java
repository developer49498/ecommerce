package com.example.isms.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AttendanceService {


    public CompletableFuture<Void> recordStudentWiseAttendance(
            String courseId,
            String date,
            Map<String, Boolean> attendanceMap) {

        return CompletableFuture.runAsync(() -> {
            Firestore firestore = FirestoreClient.getFirestore();

            try {
                for (Map.Entry<String, Boolean> entry : attendanceMap.entrySet()) {
                    String studentId = entry.getKey();
                    boolean isPresent = entry.getValue();

                    // Reference to the course doc
                    DocumentReference courseDocRef = firestore
                            .collection("student-attendance")
                            .document(studentId)
                            .collection("courseid")
                            .document(courseId);

                    // Reference to the history document (not subcollection)
                    DocumentReference historyDocRef = courseDocRef.collection("meta").document("history");

                    firestore.runTransaction(transaction -> {
                        DocumentSnapshot courseSnap = transaction.get(courseDocRef).get();

                        long totalClasses = courseSnap.contains("totalClasses")
                                ? courseSnap.getLong("totalClasses") + 1
                                : 1;

                        long presentCount = courseSnap.contains("presentCount")
                                ? courseSnap.getLong("presentCount")
                                : 0;

                        if (isPresent) {
                            presentCount += 1;
                        }

                        // Update class counts in course doc
                        Map<String, Object> classStats = new HashMap<>();
                        classStats.put("totalClasses", totalClasses);
                        classStats.put("presentCount", presentCount);

                        transaction.set(courseDocRef, classStats, SetOptions.merge());

                        // Update the history document with date as field
                        Map<String, Object> dateUpdate = new HashMap<>();
                        dateUpdate.put(date, isPresent);

                        transaction.set(historyDocRef, dateUpdate, SetOptions.merge());

                        return null;
                    });
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to record attendance: " + e.getMessage(), e);
            }
        });
    }


    public CompletableFuture<List<Map<String, Object>>> getAttendancePercentageByCourseAndBatch(
            String courseId,
            String semester,
            String batch,
            String section) {

        Firestore firestore = FirestoreClient.getFirestore();
        StudentMapperService studentMapperService=new StudentMapperService();
        return studentMapperService.getStudentIdAndNames(semester, batch, section)
                .thenCompose(studentList -> {

                    List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

                    for (Map<String, String> student : studentList) {
                        String studentId = student.get("studentId");
                        String studentName = student.get("name");

                        // Fetch attendance doc for this student + course
                        DocumentReference attendanceDocRef = firestore
                                .collection("student-attendance")
                                .document(studentId)
                                .collection("courseid")
                                .document(courseId);

                        CompletableFuture<Map<String, Object>> attendanceFuture = CompletableFuture.supplyAsync(() -> {
                            try {
                                DocumentSnapshot snap = attendanceDocRef.get().get();

                                long totalClasses = snap.contains("totalClasses") ? snap.getLong("totalClasses") : 0L;
                                long presentCount = snap.contains("presentCount") ? snap.getLong("presentCount") : 0L;

                                double attendancePercent = 0.0;
                                if (totalClasses > 0) {
                                    attendancePercent = (presentCount * 100.0) / totalClasses;
                                }

                                Map<String, Object> result = new HashMap<>();
                                result.put("studentId", studentId);
                                result.put("studentName", studentName);
                                result.put("attendancePercentage", Math.round(attendancePercent));

                                return result;
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to get attendance for student " + studentId, e);
                            }
                        });

                        futures.add(attendanceFuture);
                    }

                    // Combine all futures and return the list
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .toList());
                });
    }

    public CompletableFuture<List<Map<String, Object>>> getAttendanceByStudentAndCourse(
            String studentId,
            String courseId) {

        Firestore firestore = FirestoreClient.getFirestore();

        DocumentReference historyDocRef = firestore
                .collection("student-attendance")
                .document(studentId)
                .collection("courseid")
                .document(courseId)
                .collection("meta")
                .document("history");

        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentSnapshot snapshot = historyDocRef.get().get();
                if (!snapshot.exists()) {
                    return List.of(); // no history
                }

                Map<String, Object> fields = snapshot.getData();
                List<Map<String, Object>> result = new ArrayList<>();

                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("date", entry.getKey());
                    record.put("present", entry.getValue());
                    result.add(record);
                }

                return result;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch attendance history: " + e.getMessage(), e);
            }
        });
    }



    public CompletableFuture<List<Map<String, Object>>> getAttendanceByCourseDateAndBatch(
            String courseId,
            String semester,
            String batchYear,
            String section,
            String date) {

        Firestore firestore = FirestoreClient.getFirestore();
        StudentMapperService studentMapperService = new StudentMapperService();

        // Convert input date format (yyyy-MM-dd) to Firestore format (dd-MM-yyyy)
        String firestoreDateKey;
        try {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter firestoreFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            firestoreDateKey = LocalDate.parse(date, inputFormat).format(firestoreFormat);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd", e);
        }

        return studentMapperService.getStudentIdAndNames(semester, batchYear, section)
                .thenCompose(students -> {
                    List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

                    for (Map<String, String> student : students) {
                        String studentId = student.get("studentId");
                        String studentName = student.get("name");

                        DocumentReference historyDoc = firestore
                                .collection("student-attendance")
                                .document(studentId)
                                .collection("courseid")
                                .document(courseId)
                                .collection("meta")
                                .document("history");

                        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                            try {
                                DocumentSnapshot snapshot = historyDoc.get().get();
                                boolean isPresent = false;

                                if (snapshot.exists() && snapshot.contains(firestoreDateKey)) {
                                    Object value = snapshot.get(firestoreDateKey);
                                    if (value instanceof Boolean) {
                                        isPresent = (Boolean) value;
                                    } else {
                                        System.err.println("Unexpected type for attendance on " + firestoreDateKey + " for student " + studentId);
                                    }
                                } else {
                                    System.out.println("Date " + firestoreDateKey + " not found for student " + studentId);
                                }

                                Map<String, Object> result = new HashMap<>();
                                result.put("studentId", studentId);
                                result.put("studentName", studentName);
                                result.put("present", isPresent);
                                return result;

                            } catch (Exception e) {
                                throw new RuntimeException("Failed to get attendance for student " + studentId, e);
                            }
                        });

                        futures.add(future);
                    }

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .toList());
                });
    }


    public CompletableFuture<Map<String, Object>> getAttendanceStatsByStudentAndCourse(
            String studentId,
            String courseId) {

        Firestore firestore = FirestoreClient.getFirestore();

        DocumentReference attendanceDocRef = firestore
                .collection("student-attendance")
                .document(studentId)
                .collection("courseid")
                .document(courseId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentSnapshot snap = attendanceDocRef.get().get();

                long totalClasses = snap.contains("totalClasses") ? snap.getLong("totalClasses") : 0L;
                long presentClasses = snap.contains("presentCount") ? snap.getLong("presentCount") : 0L;
                long absentClasses = totalClasses - presentClasses;

                double percentage = (totalClasses > 0)
                        ? (presentClasses * 100.0) / totalClasses
                        : 0.0;

                Map<String, Object> stats = new HashMap<>();
                stats.put("totalClasses", totalClasses);
                stats.put("presentClasses", presentClasses);
                stats.put("absentClasses", absentClasses);
                stats.put("attendancePercentage", Math.round(percentage)); // Rounded to nearest integer

                return stats;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch attendance stats: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Map<String, Object>> getAttendanceByStudentAndDate(
            String studentId,
            String courseId,
            String inputDate) {

        Firestore firestore = FirestoreClient.getFirestore();

        // Convert yyyy-MM-dd to dd-MM-yyyy
        String formattedDate;
        try {
            LocalDate parsedDate = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd. Got: " + inputDate, e);
        }

        DocumentReference historyDocRef = firestore
                .collection("student-attendance")
                .document(studentId)
                .collection("courseid")
                .document(courseId)
                .collection("meta")
                .document("history");

        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentSnapshot snapshot = historyDocRef.get().get();
                boolean isPresent = false;

                if (snapshot.exists() && snapshot.contains(formattedDate)) {
                    Object value = snapshot.get(formattedDate);
                    if (value instanceof Boolean) {
                        isPresent = (Boolean) value;
                    } else {
                        System.err.println("Unexpected data type for attendance on " + formattedDate);
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("studentId", studentId);
                result.put("date", formattedDate);
                result.put("present", isPresent);

                return result;

            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch attendance on " + formattedDate + ": " + e.getMessage(), e);
            }
        });
    }

}
