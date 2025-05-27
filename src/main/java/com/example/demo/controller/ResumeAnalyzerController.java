package com.example.demo.controller;

import com.example.demo.service.ResumeAnalyzerService;
import com.example.demo.dto.AnalyzeRequest;
import com.example.demo.dto.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ResumeAnalyzerController {

    private final ResumeAnalyzerService analyzerService;

    public ResumeAnalyzerController(ResumeAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResponse> analyzeResumes(
            @RequestParam String jobTitle,
            @RequestParam String jobDescription,
            @RequestParam("resumes") List<MultipartFile> resumes) {

        AnalyzeResponse response = analyzerService.analyze(jobTitle, jobDescription, resumes);
        return ResponseEntity.ok(response);
    }
}


