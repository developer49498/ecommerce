package com.example.isms.controller;

import com.example.isms.model.book;
import com.example.isms.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // Endpoint to add new book
    @PostMapping("/add")
    public ResponseEntity<String> addNewBook(@RequestBody book newBook) {
        bookService.addNewBook(newBook);
        return ResponseEntity.ok("Book added successfully.");
    }

    // Endpoint to add multiple books in bulk
    @PostMapping("/add-bulk")
    public ResponseEntity<String> addBooksInBulk(@RequestBody List<book> books) {
        bookService.addBooksInBulk(books);
        return ResponseEntity.ok("Bulk books added successfully.");
    }

    // Endpoint to get book by ID
    @GetMapping("/id/{id}")
    public ResponseEntity<book> getBookById(@PathVariable String id) {
        book result = bookService.getBookById(id);
        return ResponseEntity.ok(result);
    }

    // Endpoint to get book by name
    @GetMapping("/name/{title}")
    public ResponseEntity<Map<String, Object>> getBookByName(@PathVariable String title) {
        Map<String, Object> result = bookService.getBookByName(title);
        return ResponseEntity.ok(result);
    }


    //    // Endpoint to issue a book to a student
    @PostMapping("/issue")
    public ResponseEntity<String> issueBook(@RequestParam String studentId, @RequestParam String bookId) {
        bookService.issueBook(studentId, bookId);
        return ResponseEntity.ok("Book issued to student: " + studentId);
    }
//
//    // Endpoint to return a book from a student
    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestParam String studentId, @RequestParam String bookId) {
        bookService.returnBook(studentId, bookId);
        return ResponseEntity.ok("Book returned by student: " + studentId);
    }
//
    // Endpoint to get student book details
    @GetMapping("/student/{studentId}")
    public ResponseEntity<String> getStudentBookDetails(@PathVariable String studentId) {
        String details = bookService.getStudentBookDetails(studentId);
        return ResponseEntity.ok(details);
    }
}
