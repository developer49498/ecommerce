package com.example.isms.controller;

import com.example.isms.service.OfficialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/officials")
public class OfficialController {

    @Autowired
    private OfficialService officialService;

    // GET /officials/email?notationName=HOD-CSE
    @GetMapping("/email")
    public String getEmailByNotation(@RequestParam String notationName) {
        return officialService.getEmailByNotationName(notationName);
    }

    // GET /officials/notation?email=ajaya@iiit-bh.ac.in
    @GetMapping("/notation")
    public String getNotationByEmail(@RequestParam String email) {
        return officialService.getNotationNameByEmail(email);
    }
    @GetMapping("/notations")
    public List<String> getAllNotationNames() {
        return officialService.getAllNotationNames();
    }
}
