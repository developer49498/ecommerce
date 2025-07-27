package com.example.isms.service;

import com.example.isms.model.book;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.collect.Table;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BookService {

    private final Firestore firestore = FirestoreClient.getFirestore();

    public void addNewBook(book newBook) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/books/id/" + newBook.getId();

        try {
            ResponseEntity<book> response = restTemplate.getForEntity(url, book.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Book already exists, do nothing or overwrite name if needed
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", newBook.getId());
                bookData.put("name", newBook.getName());

                firestore.collection("books").document(newBook.getId()).set(bookData);
            } else {
                // No book found, store new one
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", newBook.getId());
                bookData.put("name", newBook.getName());

                firestore.collection("books").document(newBook.getId()).set(bookData);
            }
        } catch (Exception e) {
            if (e instanceof org.springframework.web.client.HttpClientErrorException.NotFound ||
                    (e.getMessage() != null && e.getMessage().contains("404"))) {

                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", newBook.getId());
                bookData.put("name", newBook.getName());

                firestore.collection("books").document(newBook.getId()).set(bookData);
            } else {
                throw new RuntimeException("Failed to add or update book: " + e.getMessage(), e);
            }
        }
    }



    public void addBooksInBulk(List<book> books) {
        RestTemplate restTemplate = new RestTemplate();

        for (book b : books) {
            String url = "http://localhost:8080/books/id/" + b.getId();

            try {
                ResponseEntity<book> response = restTemplate.getForEntity(url, book.class);

                // Regardless of whether the book exists, we store/overwrite id and name
                Map<String, Object> bookData = new HashMap<>();
                bookData.put("id", b.getId());
                bookData.put("name", b.getName());

                firestore.collection("books").document(b.getId()).set(bookData);
            } catch (Exception e) {
                if (e instanceof org.springframework.web.client.HttpClientErrorException.NotFound ||
                        (e.getMessage() != null && e.getMessage().contains("404"))) {

                    Map<String, Object> bookData = new HashMap<>();
                    bookData.put("id", b.getId());
                    bookData.put("name", b.getName());

                    firestore.collection("books").document(b.getId()).set(bookData);
                } else {
                    throw new RuntimeException("Failed to add or update book: " + e.getMessage(), e);
                }
            }
        }
    }



    public book getBookById(String id) {
        try {
            DocumentReference docRef = firestore.collection("books").document(id);
            return docRef.get().get().toObject(book.class);
        } catch (Exception e) {
            throw new RuntimeException("Book not found with ID: " + id, e);
        }
    }


    public Map<String, Object> getBookByName(String title) {
        try {
            String lowerTitle = title.toLowerCase();

            ApiFuture<QuerySnapshot> future = firestore.collection("books").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<QueryDocumentSnapshot> matchedBooks = new ArrayList<>();
            String actualBookName = null;

            for (QueryDocumentSnapshot doc : documents) {
                String name = doc.getString("name");
                if (name != null && name.toLowerCase().equals(lowerTitle)) {
                    matchedBooks.add(doc);
                    actualBookName = name; // Store actual name from DB
                }
            }

            int availableCopies = matchedBooks.size();

            if (availableCopies > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("bookName", actualBookName); // Use stored name
                result.put("availableCopies", availableCopies);
                return result;
            } else {
                throw new RuntimeException("No book found with title: " + title);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving book by name: " + title, e);
        }
    }

    public void issueBook(String studentId, String bookId) {
        try {
            // 1. Get the book
            book bookToIssue = getBookById(bookId);

            // 4. Prepare issue info for student
            Map<String, Object> issueDetails = new HashMap<>();
            issueDetails.put("bookId", bookId);
            issueDetails.put("bookName", bookToIssue.getName());
            issueDetails.put("issueDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            // 5. Store under studentbook/{studentId}/issuedbook/{bookId}
            firestore.collection("studentbook")
                    .document(studentId)
                    .collection("issuedbook")
                    .document(bookId)
                    .set(issueDetails);

            // 6. Delete the book from 'books' collection
            firestore.collection("books")
                    .document(bookId)
                    .delete();

        } catch (Exception e) {
            throw new RuntimeException("Failed to issue book: " + e.getMessage(), e);
        }
    }




    public void returnBook(String studentId, String bookId) {
        try {
            // 1. Fetch the borrowed book details from student's record
            DocumentSnapshot issuedBookDoc = firestore.collection("studentbook")
                    .document(studentId)
                    .collection("issuedbook")
                    .document(bookId)
                    .get()
                    .get();

            if (!issuedBookDoc.exists()) {
                throw new RuntimeException("Book not found in student's borrowed list.");
            }

            String bookName = issuedBookDoc.getString("bookName");

            // 2. Recreate the book in the books collection
            Map<String, Object> bookData = new HashMap<>();
            bookData.put("id", bookId);
            bookData.put("name", bookName);

            firestore.collection("books").document(bookId).set(bookData);

            // 3. Remove the book from student's issued list
            firestore.collection("studentbook")
                    .document(studentId)
                    .collection("issuedbook")
                    .document(bookId)
                    .delete();

        } catch (Exception e) {
            throw new RuntimeException("Failed to return book: " + e.getMessage(), e);
        }
    }


    public String getStudentBookDetails(String studentId) {
        try {
            // 1. Extract programme, branch, year from studentId
            String programmeCode = studentId.substring(0, 1);
            String branchCode = studentId.substring(1, 2);
            String yearCode = studentId.substring(2, 4);

            // Programme mapping
            String programme = switch (programmeCode.toLowerCase()) {
                case "b" -> "b-tech";
                case "a" -> "m-tech";
                case "c" -> "phd";
                default -> throw new RuntimeException("Invalid programme code");
            };

            // Branch mapping
            String branch = switch (branchCode) {
                case "1" -> "CSE";
                case "2" -> "ETC";
                case "3" -> "EEE";
                case "4" -> "IT";
                case "5" -> "CE";
                default -> throw new RuntimeException("Invalid branch code");
            };

            // Enrollment year
            String year = "20" + yearCode;

            // 2. Call student details API
            String url = String.format("http://localhost:8080/api/students/searchById/%s/%s/%s/%s",
                    branch, programme, year, studentId);

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> studentData = restTemplate.getForObject(url, Map.class);

            // 3. Fetch issued book details
            CollectionReference issuedBooksRef = firestore
                    .collection("studentbook")
                    .document(studentId)
                    .collection("issuedbook");

            ApiFuture<QuerySnapshot> future = issuedBooksRef.get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            List<Map<String, Object>> issuedBooks = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                issuedBooks.add(doc.getData());
            }

            // 4. Combine and return result
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("studentId", studentId);
            response.put("name", studentData.get("name"));
            response.put("branch", branch);
            response.put("issuedBooks", issuedBooks);

            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get student book details: " + e.getMessage(), e);
        }
    }

}
