package com.example.demo.controller;

import com.example.demo.entity.JobDescription;
import com.example.demo.repository.JobDescriptionRepository;
import com.example.demo.service.JDService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JDController {
    private final JDService jDService;
    private final JobDescriptionRepository jobDescriptionRepository;

    @PostMapping(path = "add-jd")
    public ResponseEntity<Map<String , Object>> getJobDescription(@RequestBody String jD) {
        try {
            return jDService.getJobDescription(jD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping(path = "get-jd")
    public ResponseEntity<List<JobDescription>> getAllJobDescriptions() {
        try {
            List<JobDescription> allJds = jobDescriptionRepository.findAll();
            return ResponseEntity.ok(allJds);
        } catch (Exception e) {
            log.error("Error fetching JDs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

}
