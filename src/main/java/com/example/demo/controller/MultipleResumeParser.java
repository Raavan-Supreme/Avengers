package com.example.demo.controller;

import com.example.demo.dto.Response;
import com.example.demo.dto.Resume;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.service.ResumeParserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/V1/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MultipleResumeParser {

    private final ResumeParserService resumeParserService;
    private final ResumeRepository resumeRepository;

    @PostMapping
    public ResponseEntity<List<Resume>> parseResumes(@RequestParam("file") MultipartFile[] files) {
        List<Resume> responses = new ArrayList<>();
        Response response = new Response();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    Resume errorResume = new Resume();
                    errorResume.setName("Error: File is empty - " + file.getOriginalFilename());
                    responses.add(errorResume);
                    continue;
                }

                if (!file.getContentType().equals("application/pdf")) {
                    Resume errorResume = new Resume();
                    errorResume.setName("Error: Only PDF files are supported - " + file.getOriginalFilename());
                    responses.add(errorResume);
                    continue;
                }

                String result = resumeParserService.extractResumeInfo(file);
                if ((result.startsWith("'''json") || result.startsWith("```json")) &&
                        (result.endsWith("'''") || result.endsWith("```"))) {
                    result = result.replaceFirst("^('''|```)[jJ][sS][oO][nN]\\s*", "");
                    result = result.replaceFirst("('''|```)$", "");
                }

                Resume resume = mapper.readValue(result, Resume.class);
                responses.add(resume);
                resume.setEmail(resume.getPersonal_info().getEmail());
                resume.setName(resume.getPersonal_info().getName());
                resume.setPhNo(resume.getPersonal_info().getPhone());
                Resume savedResume = resumeRepository.save(resume);
                response.setResponse(responses);
            } catch (Exception e) {
                Resume errorResume = new Resume();
                errorResume.setName("Error: Failed to process " + file.getOriginalFilename() + ": " + e.getMessage());
                responses.add(errorResume);            }
        }
        return ResponseEntity.ok().body(responses);
    }

    // Similarly update for Gemini
    @PostMapping("/parse/gemini")
    public ResponseEntity<List<Object>> parseResumesWithGemini(@RequestParam("files") MultipartFile[] files) {
        List<Object> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    responses.add("{\"error\": \"File is empty: " + file.getOriginalFilename() + "\"}");
                    continue;
                }
                String result = resumeParserService.extractResumeInfoWithGemini(file);
                responses.add(result);
            } catch (Exception e) {
                responses.add("{\"error\": \"Failed to process " + file.getOriginalFilename() + ": " + e.getMessage() + "\"}");
            }
        }
        return ResponseEntity.ok(responses);
    }

    // Similarly update for Claude
    @PostMapping("/parse/claude")
    public ResponseEntity<List<Object>> parseResumesWithClaude(@RequestParam("files") MultipartFile[] files) {
        List<Object> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    responses.add("{\"error\": \"File is empty: " + file.getOriginalFilename() + "\"}");
                    continue;
                }
                String result = resumeParserService.extractResumeInfoWithClaude(file);
                responses.add(result);
            } catch (Exception e) {
                responses.add("{\"error\": \"Failed to process " + file.getOriginalFilename() + ": " + e.getMessage() + "\"}");
            }
        }
        return ResponseEntity.ok(responses);
    }

    // Similarly update for OpenAI
    @PostMapping("/parse/openai")
    public ResponseEntity<List<Object>> parseResumesWithOpenAI(@RequestParam("files") MultipartFile[] files) {
        List<Object> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    responses.add("{\"error\": \"File is empty: " + file.getOriginalFilename() + "\"}");
                    continue;
                }
                String result = resumeParserService.extractResumeInfoWithOpenAI(file);
                responses.add(result);
            } catch (Exception e) {
                responses.add("{\"error\": \"Failed to process " + file.getOriginalFilename() + ": " + e.getMessage() + "\"}");
            }
        }
        return ResponseEntity.ok(responses);
    }

    // Similarly update for LlamaParse
    @PostMapping("/parse/llamaparse")
    public ResponseEntity<List<Object>> parseResumesWithLlamaParse(@RequestParam("files") MultipartFile[] files) {
        List<Object> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    responses.add("{\"error\": \"File is empty: " + file.getOriginalFilename() + "\"}");
                    continue;
                }
                String result = resumeParserService.extractResumeInfoWithLlamaParse(file);
                responses.add(result);
            } catch (Exception e) {
                responses.add("{\"error\": \"Failed to process " + file.getOriginalFilename() + ": " + e.getMessage() + "\"}");
            }
        }
        return ResponseEntity.ok(responses);
    }
}

