package com.example.isms.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class OfficialService {

    public String getEmailByNotationName(String notationName) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference grievanceMap = db.collection("grievance_map");

        try {
            ApiFuture<QuerySnapshot> future = grievanceMap.get();
            QuerySnapshot documents = future.get();

            for (QueryDocumentSnapshot document : documents) {
                String currentNotationName = document.getString("notation_name");
                if (currentNotationName != null && currentNotationName.equalsIgnoreCase(notationName)) {
                    return document.getId(); // The document ID is the email
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null; // Not found
    }

    public String getNotationNameByEmail(String email) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference grievanceMap = db.collection("grievance_map");

        try {
            DocumentSnapshot doc = grievanceMap.document(email).get().get();
            return doc.getString("notation_name");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ✅ New method to get all notation names
    public List<String> getAllNotationNames() {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference grievanceMap = db.collection("grievance_map");

        List<String> notationNames = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = grievanceMap.get();
            QuerySnapshot documents = future.get();

            for (QueryDocumentSnapshot document : documents) {
                String notationName = document.getString("notation_name");
                if (notationName != null) {
                    notationNames.add(notationName);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return notationNames;
    }

}

