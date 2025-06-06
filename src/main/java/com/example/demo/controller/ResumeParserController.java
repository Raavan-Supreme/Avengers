package com.example.demo.controller;

import com.example.demo.dto.Resume;
import com.example.demo.service.ResumeParserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResumeParserController {

    private final ResumeParserService resumeParserService;

    @PostMapping
    public ResponseEntity<Object> parseResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Please select a file to upload\"}");
            }

            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("{\"error\": \"Only PDF files are supported\"}");
            }

            String result = resumeParserService.extractResumeInfo(file);
            if ((result.startsWith("'''json") || result.startsWith("```json")) &&
                    (result.endsWith("'''") || result.endsWith("```"))) {

                // Remove the starting marker
                result = result.replaceFirst("^('''|```)[jJ][sS][oO][nN]\\s*", "");
                // Remove the ending marker
                result = result.replaceFirst("('''|```)$", "");
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Resume resume = mapper.readValue(result, Resume.class);
            return ResponseEntity.ok(resume);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to process resume: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/parse/gemini")
    public ResponseEntity<String> parseResumeWithGemini(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Please select a file to upload\"}");
            }

            String result = resumeParserService.extractResumeInfoWithGemini(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to process resume: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/parse/claude")
    public ResponseEntity<String> parseResumeWithClaude(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Please select a file to upload\"}");
            }

            String result = resumeParserService.extractResumeInfoWithClaude(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to process resume: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/parse/openai")
    public ResponseEntity<String> parseResumeWithOpenAI(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Please select a file to upload\"}");
            }

            String result = resumeParserService.extractResumeInfoWithOpenAI(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to process resume: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/parse/llamaparse")
    public ResponseEntity<String> parseResumeWithLlamaParse(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Please select a file to upload\"}");
            }

            String result = resumeParserService.extractResumeInfoWithLlamaParse(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to process resume: " + e.getMessage() + "\"}");
        }
    }
}

