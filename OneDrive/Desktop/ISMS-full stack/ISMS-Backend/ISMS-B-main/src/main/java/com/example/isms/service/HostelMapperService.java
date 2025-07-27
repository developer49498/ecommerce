package com.example.isms.service;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HostelMapperService {

    private final Firestore firestore;

    @Autowired
    public HostelMapperService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void storeHostelMapping(List<String> studentIds, String hostel) {
        String subcollection = hostel.equalsIgnoreCase("new-hostel") ? "new" : "old";

        CollectionReference hostelCollection = firestore
                .collection("hostel-student-mapper")
                .document(subcollection)
                .collection("students");

        for (String studentId : studentIds) {
            Map<String, Object> data = new HashMap<>();
            data.put("studentId", studentId);

            hostelCollection.document(studentId).set(data);
        }
    }
}
