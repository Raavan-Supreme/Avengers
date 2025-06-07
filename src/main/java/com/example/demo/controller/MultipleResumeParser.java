package com.example.demo.controller;

import com.example.demo.dto.Response;
import com.example.demo.dto.Resume;
import com.example.demo.dto.Skills;
import com.example.demo.entity.JobDescription;
import com.example.demo.entity.JobSkills;
import com.example.demo.repository.JobDescriptionRepository;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.service.ResumeParserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MultipleResumeParser {

    private final ResumeParserService resumeParserService;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    @PostMapping
    public ResponseEntity<List<Resume>> parseResumes(@RequestParam("file") MultipartFile[] files, Long id) {
        List<Resume> responses = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JobDescription jobDescription = jobDescriptionRepository.findById(id).orElse(null);
        if (jobDescription == null || jobDescription.getSkills() == null) {
            return ResponseEntity.badRequest().body(List.of());
        }

        List<String> jdSkills = extractAllJDRequiredSkills(jobDescription.getSkills());

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    Resume errorResume = new Resume();
                    errorResume.setName("Error: File is empty - " + file.getOriginalFilename());
                    responses.add(errorResume);
                    continue;
                }

                if (!"application/pdf".equals(file.getContentType())) {
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

                List<String> resumeSkills = extractAllResumeSkills(resume.getSkills());

                double matchPercentage = calculateSkillMatchPercentage(jdSkills, resumeSkills);
                if (matchPercentage < 60.0) {
                    Resume errorResume = new Resume();
                    errorResume.setName("Rejected: Skills match below 60% - " + file.getOriginalFilename());
                    responses.add(errorResume);
                    resume.setStatus("REJECTED");
                    continue;
                }

                resume.setEmail(resume.getPersonal_info().getEmail());
                resume.setName(resume.getPersonal_info().getName());
                resume.setPhNo(resume.getPersonal_info().getPhone());
                resume.setUploadDate(LocalDateTime.now());
                resume.setStatus("ACCEPTED");
                resume.setPassingPercentage(matchPercentage);
                Resume savedResume = resumeRepository.save(resume);
                responses.add(savedResume);

            } catch (Exception e) {
                Resume errorResume = new Resume();
                errorResume.setName("Error: Failed to process " + file.getOriginalFilename() + ": " + e.getMessage());
                responses.add(errorResume);
            }
        }

        return ResponseEntity.ok(responses);
    }

// Helper methods

    private List<String> extractAllResumeSkills(Skills skills) {
        if (skills == null) return List.of();
        return Stream.of(
                        skills.getTechnical(),
                        skills.getSoft(),
                        skills.getTools(),
                        skills.getLanguages()
                ).filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(String::toLowerCase)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private List<String> extractAllJDRequiredSkills(JobSkills jobSkills) {
        if (jobSkills == null) return List.of();
        return Stream.of(
                        jobSkills.getTechnical(),
                        jobSkills.getTools(),
                        jobSkills.getMethodologies()
                ).filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(String::toLowerCase)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private double calculateSkillMatchPercentage(List<String> jdSkills, List<String> resumeSkills) {
        if (jdSkills.isEmpty()) return 0.0;
        long matched = jdSkills.stream()
                .filter(resumeSkills::contains)
                .count();
        return (matched * 100.0) / jdSkills.size();
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

