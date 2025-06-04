package com.example.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ResumeParserService {

    // Claude API (Anthropic) - Excellent PDF reading capabilities
    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;
    private final String anthropicApiUrl = "https://api.anthropic.com/v1/messages";

    // Google Gemini API - Free tier with PDF support
    @Value("${google.gemini.api.key:}")
    private String geminiApiKey;
    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    // OpenAI GPT-4 Vision (if you have credits)
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    private final String openaiApiUrl = "https://api.openai.com/v1/chat/completions";

    // LlamaParse (Free tier) - Specialized for document parsing
    @Value("${llamaparse.api.key:}")
    private String llamaParseApiKey;
    private final String llamaParseApiUrl = "https://api.cloud.llamaindex.ai/api/parsing";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Method 1: Using Google Gemini (FREE and excellent PDF support)
    public String extractResumeInfoWithGemini(MultipartFile pdfFile) throws IOException, InterruptedException {
            if (geminiApiKey == null || geminiApiKey.isEmpty()) {
                return "{\"error\": \"Google Gemini API key not configured\"}";
            }

            // Convert PDF to base64
            String base64Pdf = Base64.getEncoder().encodeToString(pdfFile.getBytes());

            Map<String, Object> payload = new HashMap<>();

            // Content structure for Gemini
            Map<String, Object> part1 = new HashMap<>();
        part1.put("text", """
                 You are a professional resume parser. Analyze this PDF resume and extract all information strictly in the following JSON format:
                    {
                       "personal_info": {
                        "name": "Full Name",
                        "email": "email@example.com",
                        "phone": "phone number",
                        "address": "full address",
                        "linkedin": "LinkedIn URL",
                        "portfolio": "Portfolio/Website URL"
                   },
                   "summary": "Professional summary or objective",
                   "skills": {
                            "technical": ["skill1", "skill2"],
                            "soft": ["skill1", "skill2"],
                            "tools": ["tool1", "tool2"],
                            "languages": ["language1", "language2"]
                   },
                   "experience": [
                     {
                       "company": "Company Name",
                       "position": "Job Title",
                       "duration": "Start Date - End Date",
                       "location": "City, Country",
                       "responsibilities": ["responsibility1", "responsibility2"],
                       "achievements": ["achievement1", "achievement2"]
                     }
                   ],
                   "education": [
                     {
                       "institution": "School/University Name",
                       "degree": "Degree Type and Major",
                       "graduation_year": "Year",
                       "gpa": "GPA if available",
                       "location": "City, Country"
                     }
                   ],
                   "certifications": [
                     {
                       "name": "Certification Name",
                       "issuer": "Issuing Organization",
                       "date": "Date Obtained",
                       "expiry": "Expiry Date if applicable"
                     }
                   ],
                   "projects": [
                     {
                       "name": "Project Name",
                       "description": "Project Description",
                       "technologies": ["tech1", "tech2"],
                       "duration": "Project Duration"
                     }
                   ],
                   "achievements": ["achievement1", "achievement2"],
                   "additional_info": {
                     "hobbies": ["hobby1", "hobby2"],
                     "volunteer": ["volunteer experience"],
                     "references": "References information",
                     "extra_info": {
                        "any_other_fields_not_matching_above": "their values"
                     }
                   }
                 }
                
                 🔸 Correct typos and normalize formatting (e.g., "MySql" → "MySQL", "Reactjs" → "React.js").
                 🔸 Map the information strictly to this structure. If any field doesn’t fit any above category, add it into the nested 'additional_info.extra_info' map.
                 🔸 Only return the JSON strictly in the specified structure—no extra text or explanation.
                 🔸 Be thorough and complete, extracting all available information from the resume PDF.
                
                 Please strictly follow this format.
                """);

//            part1.put("text", """
//                You are a professional resume parser. Analyze this PDF resume and extract all information in the following JSON format:
//                {
//                  "personal_info": {
//                    "name": "Full Name",
//                    "email": "email@example.com",
//                    "phone": "phone number",
//                    "address": "full address",
//                    "linkedin": "LinkedIn URL",
//                    "portfolio": "Portfolio/Website URL"
//                  },
//                  "summary": "Professional summary or objective",
//                  "skills": {
//                    "technical": ["skill1", "skill2"],
//                    "soft": ["skill1", "skill2"],
//                    "tools": ["tool1", "tool2"],
//                    "languages": ["language1", "language2"]
//                  },
//                  "experience": [
//                    {
//                      "company": "Company Name",
//                      "position": "Job Title",
//                      "duration": "Start Date - End Date",
//                      "location": "City, Country",
//                      "responsibilities": ["responsibility1", "responsibility2"],
//                      "achievements": ["achievement1", "achievement2"]
//                    }
//                  ],
//                  "education": [
//                    {
//                      "institution": "School/University Name",
//                      "degree": "Degree Type and Major",
//                      "graduation_year": "Year",
//                      "gpa": "GPA if available",
//                      "location": "City, Country"
//                    }
//                  ],
//                  "certifications": [
//                    {
//                      "name": "Certification Name",
//                      "issuer": "Issuing Organization",
//                      "date": "Date Obtained",
//                      "expiry": "Expiry Date if applicable"
//                    }
//                  ],
//                  "projects": [
//                    {
//                      "name": "Project Name",
//                      "description": "Project Description",
//                      "technologies": ["tech1", "tech2"],
//                      "duration": "Project Duration"
//                    }
//                  ],
//                  "achievements": ["achievement1", "achievement2"],
//                  "additional_info": {
//                    "hobbies": ["hobby1", "hobby2"],
//                    "volunteer": ["volunteer experience"],
//                    "references": "References information"
//                  }
//                }
//                🔸 Correct typos and normalize formatting (e.g., "MySql" → "MySQL", "Reactjs" → "React.js").
//                🔸 Map the information strictly to this structure. If a field doesn’t fit any category, add it to `additionalInfo.extra_info`.
//                🔸 Only return the JSON in the specified structure—no extra text or formatting.
//                🔸 Be thorough and complete.
//
//                Please be thorough and extract all available information from the resume PDF.
//                """);

            Map<String, Object> part2 = new HashMap<>();
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", "application/pdf");
            inlineData.put("data", base64Pdf);
            part2.put("inline_data", inlineData);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part1, part2));

            payload.put("contents", List.of(content));

            // Generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("maxOutputTokens", 4000);
            payload.put("generationConfig", generationConfig);

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geminiApiUrl + "?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error: {}", response.body());
                return "{\"error\": \"Gemini API call failed: " + response.body() + "\"}";
            }

            JsonNode result = objectMapper.readTree(response.body());
            return result.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    // Method 2: Using Claude (Anthropic) - Excellent document understanding
    public String extractResumeInfoWithClaude(MultipartFile pdfFile) throws IOException, InterruptedException {
            if (anthropicApiKey == null || anthropicApiKey.isEmpty()) {
                return "{\"error\": \"Anthropic API key not configured\"}";
            }

            // Convert PDF to base64
            String base64Pdf = Base64.getEncoder().encodeToString(pdfFile.getBytes());

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "claude-3-sonnet-20240229");
            payload.put("max_tokens", 4000);
            payload.put("temperature", 0.1);

            // Message content with PDF
            Map<String, Object> content1 = new HashMap<>();
            content1.put("type", "text");
            content1.put("text", """
              You are a professional and intelligent resume parser. Analyze this PDF resume and extract all information into the following **JSON format**.\s
              Make sure to:
                - **Correct any typos** and **standardize formatting** (for example: "MySql" → "MySQL", "Reactjs" → "React.js", "Nodejs" → "Node.js", "Java script" → "JavaScript").
                - **Return clean and consistent field names** and **avoid spelling mistakes**.
                - **Follow the provided JSON structure strictly** and return a well-formatted JSON.
              Please analyze this resume PDF and extract all information in a structured JSON format. Include:
                - Personal information (name, contact details, links)
                - Professional summary
                - Skills (categorized by type)
                - Work experience (with detailed responsibilities and achievements)
                - Education (with all academic details)
                - Certifications
                - Projects
                - Achievements and awards
                - Additional information (hobbies, volunteer work, etc.)
                
               Be thorough and maintain the original formatting context from the PDF.
               """);

            Map<String, Object> content2 = new HashMap<>();
            content2.put("type", "document");
            content2.put("source", Map.of(
                    "type", "base64",
                    "media_type", "application/pdf",
                    "data", base64Pdf
            ));

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", List.of(content1, content2));

            payload.put("messages", List.of(message));

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(anthropicApiUrl))
                    .header("x-api-key", anthropicApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Claude API error: {}", response.body());
                return "{\"error\": \"Claude API call failed: " + response.body() + "\"}";
            }

            JsonNode result = objectMapper.readTree(response.body());
            return result.path("content").get(0).path("text").asText();
    }

    // Method 3: Using OpenAI GPT-4V (if you have credits)
    public String extractResumeInfoWithOpenAI(MultipartFile pdfFile) throws IOException, InterruptedException {
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                return "{\"error\": \"OpenAI API key not configured\"}";
            }

            // Convert PDF to base64
            String base64Pdf = Base64.getEncoder().encodeToString(pdfFile.getBytes());

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "gpt-4-vision-preview");
            payload.put("max_tokens", 4000);
            payload.put("temperature", 0.1);

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a professional resume parser. Extract comprehensive information from resume documents and format as structured JSON.");

            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", """
                Analyze this resume PDF and extract all information in detailed JSON format including:
                personal info, summary, skills, experience, education, certifications, projects, achievements, and additional info.
                Preserve all formatting context and be thorough.
                """);

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            imageContent.put("image_url", Map.of(
                    "url", "data:application/pdf;base64," + base64Pdf
            ));

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", List.of(textContent, imageContent));

            payload.put("messages", List.of(systemMessage, userMessage));

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(openaiApiUrl))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenAI API error: {}", response.body());
                return "{\"error\": \"OpenAI API call failed: " + response.body() + "\"}";
            }

            JsonNode result = objectMapper.readTree(response.body());
            return result.path("choices").get(0).path("message").path("content").asText();
    }

    // Method 4: Using LlamaParse (Specialized for document parsing)
    public String extractResumeInfoWithLlamaParse(MultipartFile pdfFile) throws IOException, InterruptedException {
            if (llamaParseApiKey == null || llamaParseApiKey.isEmpty()) {
                return "{\"error\": \"LlamaParse API key not configured\"}";
            }

            // Step 1: Upload PDF to LlamaParse
            String boundary = "----formdata-" + System.currentTimeMillis();
            String CRLF = "\r\n";

            StringBuilder requestBody = new StringBuilder();
            requestBody.append("--").append(boundary).append(CRLF);
            requestBody.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(pdfFile.getOriginalFilename()).append("\"").append(CRLF);
            requestBody.append("Content-Type: application/pdf").append(CRLF);
            requestBody.append(CRLF);

            byte[] pdfBytes = pdfFile.getBytes();
            byte[] beforePdf = requestBody.toString().getBytes();
            byte[] afterPdf = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

            byte[] fullBody = new byte[beforePdf.length + pdfBytes.length + afterPdf.length];
            System.arraycopy(beforePdf, 0, fullBody, 0, beforePdf.length);
            System.arraycopy(pdfBytes, 0, fullBody, beforePdf.length, pdfBytes.length);
            System.arraycopy(afterPdf, 0, fullBody, beforePdf.length + pdfBytes.length, afterPdf.length);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llamaParseApiUrl + "/upload"))
                    .header("Authorization", "Bearer " + llamaParseApiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fullBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("LlamaParse API error: {}", response.body());
                return "{\"error\": \"LlamaParse API call failed: " + response.body() + "\"}";
            }

            JsonNode uploadResult = objectMapper.readTree(response.body());
            String jobId = uploadResult.path("id").asText();

            // Step 2: Get parsed result
            Thread.sleep(5000); // Wait for processing

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(llamaParseApiUrl + "/job/" + jobId + "/result/json"))
                    .header("Authorization", "Bearer " + llamaParseApiKey)
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() != 200) {
                return "{\"error\": \"Failed to get parsing result\"}";
            }

            // The parsed text can then be sent to any LLM for structured extraction
            String parsedText = getResponse.body();
            return "{\"parsed_content\": " + objectMapper.writeValueAsString(parsedText) + "}";
    }

    // Main method - tries different services in order of preference
//    public String extractResumeInfo(MultipartFile pdfFile) {
//        //  Google Gemini first (FREE and excellent PDF support)
//        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
//            log.info("Using Google Gemini for PDF processing");
//            return extractResumeInfoWithGemini(pdfFile);
//        }
//
//        //  Claude (excellent document understanding)
//        if (anthropicApiKey != null && !anthropicApiKey.isEmpty()) {
//            log.info("Using Claude for PDF processing");
//            return extractResumeInfoWithClaude(pdfFile);
//        }
//
//        //  OpenAI
//        if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
//            log.info("Using OpenAI GPT-4V for PDF processing");
//            return extractResumeInfoWithOpenAI(pdfFile);
//        }
//
//        //  LlamaParse
//        if (llamaParseApiKey != null && !llamaParseApiKey.isEmpty()) {
//            log.info("Using LlamaParse for PDF processing");
//            return extractResumeInfoWithLlamaParse(pdfFile);
//        }
//
//        return "{\"error\": \"No PDF-capable AI service configured. Please add API keys for Gemini, Claude, OpenAI, or LlamaParse.\"}";
//    }
    public String extractResumeInfo(MultipartFile pdfFile) {
        // Google Gemini first (FREE and excellent PDF support)
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                log.info("Using Google Gemini for PDF processing");
                return extractResumeInfoWithGemini(pdfFile);
            } catch (Exception e) {
                log.warn("Gemini processing failed, trying next service: " + e.getMessage());
            }
        }

        // Claude (excellent document understanding)
        if (anthropicApiKey != null && !anthropicApiKey.isEmpty()) {
            try {
                log.info("Using Claude for PDF processing");
                return extractResumeInfoWithClaude(pdfFile);
            } catch (Exception e) {
                log.warn("Claude processing failed, trying next service: " + e.getMessage());
            }
        }

        // OpenAI GPT-4V
        if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
            try {
                log.info("Using OpenAI GPT-4V for PDF processing");
                return extractResumeInfoWithOpenAI(pdfFile);
            } catch (Exception e) {
                log.warn("OpenAI processing failed, trying next service: " + e.getMessage());
            }
        }

        // LlamaParse
        if (llamaParseApiKey != null && !llamaParseApiKey.isEmpty()) {
            try {
                log.info("Using LlamaParse for PDF processing");
                return extractResumeInfoWithLlamaParse(pdfFile);
            } catch (Exception e) {
                log.warn("LlamaParse processing failed: " + e.getMessage());
            }
        }

        return "{\"error\": \"Failed to process resume: No available AI service succeeded.\"}";
    }

}

