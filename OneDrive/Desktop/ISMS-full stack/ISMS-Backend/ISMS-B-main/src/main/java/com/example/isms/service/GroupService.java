package com.example.isms.service;


import com.example.isms.model.Group;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class GroupService {

    public String uploadSingleGroup(Group group) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("groups").document(group.getGroupName());
        ApiFuture<WriteResult> result = docRef.set(group);
        return result.get().getUpdateTime().toString();
    }

    public String uploadBulkGroups(List<Group> groups) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        WriteBatch batch = db.batch();

        for (Group group : groups) {
            DocumentReference docRef = db.collection("groups").document(group.getGroupName());
            batch.set(docRef, group);
        }

        ApiFuture<List<WriteResult>> future = batch.commit();
        future.get(); // wait for completion
        return "Bulk upload successful";
    }

    public Group getGroupByName(String groupName) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("groups").document(groupName);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(Group.class);
        } else {
            return null;
        }
    }

}
