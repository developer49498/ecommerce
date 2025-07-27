package com.example.isms.controller;

import com.example.isms.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @PostMapping("/publish")
    public String publishResult(@RequestParam String semester,
                                @RequestParam String branch,
                                @RequestParam String program,
                                @RequestParam String batchYear) {
        resultService.publishResult(semester, branch, program, batchYear);
        return "Result published.";
    }

    @PostMapping("/unpublish")
    public String unpublishResult(@RequestParam String semester,
                                  @RequestParam String branch,
                                  @RequestParam String program,
                                  @RequestParam String batchYear) {
        resultService.unpublishResult(semester, branch, program, batchYear);
        return "Result unpublished.";
    }

    @GetMapping("/isPublished")
    public boolean isPublished(@RequestParam String semester,
                               @RequestParam String branch,
                               @RequestParam String program,
                               @RequestParam String batchYear) throws Exception {
        return resultService.isResultPublished(semester, branch, program, batchYear);
    }

    @GetMapping("/published-semesters")
    public ResponseEntity<?> getPublishedSemesters(
            @RequestParam String branch,
            @RequestParam String batchYear,
            @RequestParam String program) {
        try {
            List<String> semesters = resultService.getPublishedSemesters(branch, batchYear, program);
            return ResponseEntity.ok(semesters);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching published semesters");
        }
    }
}
