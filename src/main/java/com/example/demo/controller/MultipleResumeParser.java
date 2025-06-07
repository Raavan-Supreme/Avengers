package com.example.demo.controller;

import com.example.demo.dto.Response;
import com.example.demo.dto.Resume;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.service.ResumeParserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MultipleResumeParser {

    private final ResumeParserService resumeParserService;
    private final ResumeRepository resumeRepository;

    @PostMapping
    public ResponseEntity<List<Resume>> parseResumes(@RequestParam("file") MultipartFile[] files, Long id) {
        List<Resume> responses = new ArrayList<>();
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

                if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
                    Resume errorResume = new Resume();
                    errorResume.setName("Error: Only PDF files are supported - " + file.getOriginalFilename());
                    responses.add(errorResume);
                    continue;
                }

                // Extract resume info JSON string from service
                String result = resumeParserService.extractResumeInfo(file, id);

                // Remove any markdown JSON markers if present
                if ((result.startsWith("'''json") || result.startsWith("```json")) &&
                        (result.endsWith("'''") || result.endsWith("```"))) {
                    result = result.replaceFirst("^('''|```)[jJ][sS][oO][nN]\\s*", "");
                    result = result.replaceFirst("('''|```)$", "");
                }

                // Parse the JSON string to JsonNode to check compatibility
                JsonNode jsonNode = mapper.readTree(result);
                String compatibilityStr = jsonNode.path("compatibility").asText().replace("%", "");

                int compatibility = 0;
                try {
                    compatibility = Integer.parseInt(compatibilityStr);
                } catch (NumberFormatException e) {
                    Resume errorResume = new Resume();
                    errorResume.setName("Error: Invalid compatibility format in resume - " + file.getOriginalFilename());
                    responses.add(errorResume);
                    continue;
                }

                if (compatibility < 60) {
                    Resume rejectedResume = new Resume();
                    rejectedResume.setName(jsonNode.path("personal_info").path("name").asText("Unknown"));
                    rejectedResume.setEmail(jsonNode.path("personal_info").path("email").asText(""));
                    rejectedResume.setPhNo(jsonNode.path("personal_info").path("phone").asText(""));
                    rejectedResume.setUploadDate(LocalDateTime.now());
                    rejectedResume.setCompatibility(compatibilityStr + "%");
                    rejectedResume.setStatus("rejected");
                    rejectedResume.setRejectionReason("Compatibility below threshold (" + compatibilityStr + "%)");
                    responses.add(rejectedResume);
                    continue;  // skip saving rejected resumes
                }

                // Deserialize into Resume object
                Resume resume = mapper.treeToValue(jsonNode, Resume.class);

                // Set some additional fields for convenience and db
                resume.setEmail(resume.getPersonal_info() != null ? resume.getPersonal_info().getEmail() : null);
                resume.setName(resume.getPersonal_info() != null ? resume.getPersonal_info().getName() : null);
                resume.setPhNo(resume.getPersonal_info() != null ? resume.getPersonal_info().getPhone() : null);
                resume.setUploadDate(LocalDateTime.now());
                resume.setCompatibility(compatibilityStr + "%");
                resume.setStatus("accepted");

                // Save to database
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


    // Similarly update for Gemini
    @PostMapping("/parse/gemini")
    public ResponseEntity<List<Object>> parseResumesWithGemini(@RequestParam("files") MultipartFile[] files,
                                                               Long jd) {
        List<Object> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    responses.add("{\"error\": \"File is empty: " + file.getOriginalFilename() + "\"}");
                    continue;
                }
                String result = resumeParserService.extractResumeInfoWithGemini(file, jd);
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

