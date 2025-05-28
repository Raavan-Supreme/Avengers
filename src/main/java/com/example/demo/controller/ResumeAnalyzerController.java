package com.example.demo.controller;

import com.example.demo.service.Services;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ResumeAnalyzerController {

    private final Services analyzerService;

    public ResumeAnalyzerController(Services analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> analyzeResumes(
            @RequestParam String jobTitle,
            @RequestParam String jobDescription,
            @RequestParam("resumes") MultipartFile resumes) throws IOException, InterruptedException {

        String response = analyzerService.extractResumeInfo(resumes);
        return ResponseEntity.ok(response);
    }
}


