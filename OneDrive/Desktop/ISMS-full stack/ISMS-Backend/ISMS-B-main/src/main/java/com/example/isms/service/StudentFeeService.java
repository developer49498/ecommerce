package com.example.isms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentFeeService {

private final Firestore firestore;



    public StudentFeeService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void storeFeeForFilteredStudents(String programme, String branch, String hostel, int semester,
                                            Map<String, Object> feeData) throws Exception {

        // Map of branch codes to branch names (your logic)
        Map<Integer, String> branchMap = Map.of(
                1, "CSE",
                2, "ETC",
                3, "EEE",
                4, "IT",
                5, "CE"
        );

        // Reverse map: branch name -> branch code
        Map<String, Integer> branchNameToCode = branchMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));

        // Step 1: Convert programme string to char
        char programmeChar = switch (programme.toLowerCase()) {
            case "b-tech" -> 'b';
            case "m-tech" -> 'a';
            case "ph.d"   -> 'c';
            default -> throw new IllegalArgumentException("Invalid program: " + programme);
        };

        // Step 2: Get branch code from branch name
        Integer branchCode = branchNameToCode.get(branch.toUpperCase());
        if (branchCode == null) {
            throw new IllegalArgumentException("Invalid branch name: " + branch);
        }

        // Step 3: Fetch students from Firestore
        List<String> matchingStudentIds = new ArrayList<>();
        CollectionReference hostelRef = firestore
                .collection("hostel-student-mapper")
                .document(hostel)
                .collection("students");

        List<QueryDocumentSnapshot> docs = hostelRef.get().get().getDocuments();

        for (QueryDocumentSnapshot doc : docs) {
            String studentId = doc.getId();

            if (studentId.length() < 2) continue;

            char idProgramChar = studentId.charAt(0);
            char idBranchChar = studentId.charAt(1);
            int idBranchCode = Character.getNumericValue(idBranchChar);

            if (idProgramChar == programmeChar && idBranchCode == branchCode) {
                matchingStudentIds.add(studentId);
            }
        }

        // Step 4: Store fee info for matching students
        for (String studentId : matchingStudentIds) {
            DocumentReference semDocRef = firestore
                    .collection("student_fee")
                    .document(studentId)
                    .collection("sem" + semester)
                    .document("data");

            semDocRef.set(feeData);

            // Update outstanding amount
            DocumentReference feeRootDoc = firestore.collection("student_fee").document(studentId);
            Long totalFee = feeData.get("totalAmount") instanceof Number
                    ? ((Number) feeData.get("totalAmount")).longValue()
                    : 0L;

            DocumentSnapshot doc = feeRootDoc.get().get();
            if (doc.exists() && doc.contains("outstandingAmount")) {
                Long prevOutstanding = doc.getLong("outstandingAmount");
                if (prevOutstanding != null) {
                    totalFee += prevOutstanding;
                }
            }

            feeRootDoc.set(Map.of("outstandingAmount", totalFee), SetOptions.merge());
        }
    }


    public void updateOutstandingAmount(String studentId, long newOutstanding) {
        DocumentReference docRef = firestore.collection("student_fee").document(studentId);
        docRef.set(Map.of("outstandingAmount", newOutstanding), SetOptions.merge());
    }


    public Map<String, Object> getFeeDataForStudentAsMap(String studentId, int semester) throws Exception {
        DocumentReference semDocRef = firestore
                .collection("student_fee")
                .document(studentId)
                .collection("sem" + semester)
                .document("data");

        DocumentSnapshot documentSnapshot = semDocRef.get().get();

        if (!documentSnapshot.exists()) {
            throw new Exception("No fee data found for student " + studentId + " in semester " + semester);
        }

        Map<String, Object> feeData = documentSnapshot.getData();
        if (feeData == null) {
            throw new Exception("Fee data is null for student " + studentId + " in semester " + semester);
        }

        // Rearranging: totalAmount first, rest sorted alphabetically
        Object totalAmount = feeData.remove("totalAmount");

        Map<String, Object> sortedMap = new LinkedHashMap<>();
        if (totalAmount != null) {
            sortedMap.put("totalAmount", totalAmount);
        }

        feeData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap;
    }

    public Long getOutstandingAmountForStudent(String studentId) throws Exception {
        DocumentReference docRef = firestore.collection("student_fee").document(studentId);
        DocumentSnapshot documentSnapshot = docRef.get().get();

        if (!documentSnapshot.exists() || !documentSnapshot.contains("outstandingAmount")) {
            throw new Exception("Outstanding amount not found for student: " + studentId);
        }

        Long outstandingAmount = documentSnapshot.getLong("outstandingAmount");
        if (outstandingAmount == null) {
            throw new Exception("Outstanding amount is null for student: " + studentId);
        }

        return outstandingAmount;
    }



    public void bulkUpdateOutstandingAmounts(List<Map<String, Object>> updates) throws Exception {
        for (Map<String, Object> update : updates) {
            Object idObj = update.get("studentId");
            Object amountObj = update.get("outstandingAmount");

            if (idObj == null || amountObj == null) {
                throw new IllegalArgumentException("Each update must contain 'studentId' and 'outstandingAmount'");
            }

            String studentId = idObj.toString();
            long outstandingAmount;

            try {
                if (amountObj instanceof Number) {
                    outstandingAmount = ((Number) amountObj).longValue();
                } else {
                    outstandingAmount = Long.parseLong(amountObj.toString());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid outstanding amount for student: " + studentId);
            }

            DocumentReference docRef = firestore.collection("student_fee").document(studentId);
            docRef.set(Map.of("outstandingAmount", outstandingAmount), SetOptions.merge());
        }
    }


}
