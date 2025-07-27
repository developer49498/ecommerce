package com.example.isms.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ResultService {

    @Autowired
    private Firestore firestore;

    private String getDocumentPath(String program, String branch, String batchYear, String semester) {
        return String.format("semester-result/%s_%s_%s/semesters/%s", program, branch, batchYear, semester);
    }

    public void publishResult(String semester, String branch, String program, String batchYear) {
        String docPath = getDocumentPath(program, branch, batchYear, semester);
        DocumentReference docRef = firestore.document(docPath);

        Map<String, Object> data = new HashMap<>();
        data.put("publish", true);

        ApiFuture<?> result = docRef.set(data, com.google.cloud.firestore.SetOptions.merge());
        // You may want to handle future.get() with try-catch if you want to ensure write success
    }

    public void unpublishResult(String semester, String branch, String program, String batchYear) {
        String docPath = getDocumentPath(program, branch, batchYear, semester);
        DocumentReference docRef = firestore.document(docPath);

        Map<String, Object> data = new HashMap<>();
        data.put("publish", false);

        ApiFuture<?> result = docRef.set(data, com.google.cloud.firestore.SetOptions.merge());
    }

    public boolean isResultPublished(String semester, String branch, String program, String batchYear) throws Exception {
        String docPath = getDocumentPath(program, branch, batchYear, semester);
        DocumentReference docRef = firestore.document(docPath);

        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Boolean publish = document.getBoolean("publish");
            return publish != null && publish;
        }

        return false; // Default to false if document doesn't exist
    }

    public List<String> getPublishedSemesters(String branch, String batchYear, String program) throws InterruptedException, ExecutionException {
        String basePath = String.format("semester-result/%s_%s_%s/semesters", program, branch, batchYear);
        CollectionReference semestersCollection = firestore.collection(basePath);

        // Query documents where publish == true
        ApiFuture<QuerySnapshot> query = semestersCollection.whereEqualTo("publish", true).get();

        QuerySnapshot querySnapshot = query.get();
        List<String> publishedSemesters = new ArrayList<>();

        for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
            publishedSemesters.add(doc.getId());
        }

        return publishedSemesters;
    }
}
