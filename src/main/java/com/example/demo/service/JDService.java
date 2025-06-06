package com.example.demo.service;

import com.example.demo.entity.JobDescription;
import com.example.demo.repository.JobDescriptionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class JDService {

    private final ObjectMapper objectMapper;
    private final JobDescriptionRepository jobDescriptionRepository;

    @Value("${google.gemini.api.key:}")
    private String geminiApiKey;
    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final HttpClient httpClient = HttpClient.newHttpClient();


    public ResponseEntity<Map<String, Object>> getJobDescription(String jd) {
        try {
            String prompt = """
                    You are a professional job description parser. Extract only the following information from the given job description and return it strictly in this JSON format:
                    
                    {
                      "title": "Job Title",
                      "experience_required": "Experience Required",
                      "skills": {
                        "technical": ["skill1", "skill2"],
                        "tools": ["tool1", "tool2"],
                        "methodologies": ["method1", "method2"]
                      }
                    }
                    
                    🔸 Normalize technologies (e.g., Reactjs → React.js, MySql → MySQL).
                    🔸 Map skills into appropriate categories: technical, tools, and methodologies.
                    🔸 Only return valid JSON in the structure above, no extra text.
                    JD: 
                    """ + jd;

            // Prepare Gemini prompt request
            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", List.of(content));
            payload.put("generationConfig", Map.of(
                    "temperature", 0.2,
                    "maxOutputTokens", 800
            ));

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geminiApiUrl + "?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error: {}", response.body());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Gemini API call failed: " + response.body()));
            }

            String resultText = objectMapper.readTree(response.body())
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
            String cleanedJson = resultText
                    .replaceAll("(?i)```json", "")
                    .replaceAll("```", "")
                    .trim();
            JobDescription jobDescription = objectMapper.readValue(cleanedJson, JobDescription.class);
            jobDescriptionRepository.save(jobDescription);
            Map<String, Object> resultMap = objectMapper.readValue(cleanedJson, new TypeReference<>() {});
            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {
            log.error("Error generating JD", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
