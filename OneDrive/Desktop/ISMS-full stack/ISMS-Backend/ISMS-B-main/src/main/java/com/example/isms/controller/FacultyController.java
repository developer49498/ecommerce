package com.example.isms.controller;

import com.example.isms.model.Faculty;
import com.example.isms.service.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    // Add bulk faculty
    @PostMapping("/add_bulk")
    public String addBulkFaculty(@RequestBody List<Faculty> facultyList) throws ExecutionException, InterruptedException {
        return facultyService.saveBulkFaculty(facultyList);
    }



    // Get faculty by ID
    @GetMapping("/id/{id}")
    public Faculty getFacultyById(@PathVariable String id) throws ExecutionException, InterruptedException {
        return (Faculty) facultyService.getFacultyById(id);
    }

    // Get faculty by name
    @GetMapping("/name/{name}")
    public
    List<Faculty> getFacultyByName(@PathVariable String name) throws ExecutionException, InterruptedException {
        return facultyService.searchFacultyByName(name);
    }

    // Get faculty by department
    @GetMapping("/department/{department}")
    public
    List<Faculty> getFacultyByDepartment(@PathVariable String department) throws ExecutionException, InterruptedException {
        return facultyService.getFacultyByDepartment(department);
    }

    // ✅ Get faculty by email
    @GetMapping("/email/{email}")
    public Faculty getFacultyByEmail(@PathVariable String email) throws ExecutionException, InterruptedException {
        return facultyService.getFacultyByEmail(email);
    }

    // ✅ Get faculty by administrative position
    @GetMapping("/position/{position}")
    public List<Faculty> getFacultyByAdministrativePosition(@PathVariable String position) throws ExecutionException, InterruptedException {
        return facultyService.getFacultyByAdministrativePosition(position);
    }

    // ✅ Get all faculty across all departments
    @GetMapping("/all")
    public List<Faculty> getAllFaculty() throws ExecutionException, InterruptedException {
        return facultyService.getAllFaculty();
    }

}
