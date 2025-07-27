package com.example.isms.controller;

import com.example.isms.model.HostelMappingRequest;
import com.example.isms.service.HostelMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostel-mapper")
public class HostelMapperController {

    @Autowired
    private HostelMapperService hostelMapperService;

    @PostMapping("/map-students")
    public ResponseEntity<String> mapStudentsToHostel(
            @RequestParam("hostel") String hostel,
            @RequestBody List<String> studentIds) {
        hostelMapperService.storeHostelMapping(studentIds, hostel);
        return ResponseEntity.ok("Student IDs mapped to " + hostel + " successfully.");
    }
}
