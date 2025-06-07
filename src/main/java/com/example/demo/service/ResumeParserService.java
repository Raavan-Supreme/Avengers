package com.example.demo.service;

import com.example.demo.entity.JobDescription;
import com.example.demo.repository.JobDescriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParserService {
        private final JobDescriptionRepository jobDescriptionRepository;
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
    public String extractResumeInfoWithGemini(MultipartFile pdfFile, Long jdId) throws IOException, InterruptedException {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return "{\"error\": \"Google Gemini API key not configured\"}";
        }

        // Convert PDF to base64
        String base64Pdf = Base64.getEncoder().encodeToString(pdfFile.getBytes());

        Optional<JobDescription> jobDescription = jobDescriptionRepository.findById(jdId);

        // Static Compatibility Criteria (generalized) - all % replaced with %%%% for String.format
        String staticCriteria =
                "Use the following static compatibility criteria to evaluate how well a resume matches the job description:\n\n" +
                        "🔹 Compatibility Criteria (Total: 95%%%%):\n" +
                        "- The resume covers at least 70%%%% of the technical skills listed in the job description \u2192 40%%%%\n" +
                        "- The resume includes at least 50%%%% of the tools (e.g., frameworks, libraries, platforms) mentioned in the JD \u2192 25%%%%\n" +
                        "- The resume refers to at least one of the methodologies or practices stated in the JD (e.g., Agile, Scrum, DevOps) \u2192 10%%%%\n" +
                        "- The resume summary or experience section includes a job title or role similar to the one in the JD \u2192 10%%%%\n" +
                        "- The resume is well-structured, complete, and organized according to the expected JSON schema \u2192 10%%%%\n\n" +
                        "📌 Always assign the compatibility score based on these criteria and return it as a string percentage (e.g., \"85%%%%\") in the 'compatibility' field.\n\n" +

                        "🔸 Additionally, return a field named 'criteria' in the JSON with a detailed explanation of why the resume passed or was rejected based on the compatibility criteria.\n" +
                        "For example:\n" +
                        "  - \"Passed because the resume covers 80%%%% technical skills, 60%%%% tools, mentions Agile methodology, and matches job title.\"\n" +
                        "  - \"Rejected because the resume lacks enough matching tools and does not mention required methodologies.\"\n\n" +
                        "🔸 The JSON must strictly include the fields: 'compatibility' (string percentage) and 'criteria' (explanation string).\n" +
                        "🔸 Return the full JSON strictly in the specified structure—no extra text or explanation outside the JSON." +
                        "Use only the criteria i provided if am asking for one technlogy developer it should be only that technology developer not other";

        String textTemplate =
                "You are a professional resume parser. Analyze this PDF resume and extract all information strictly in the following JSON format:\n" +
                        "{\n" +
                        "  \"personal_info\": { \"name\": \"\", \"email\": \"\", \"phone\": \"\", \"address\": \"\", \"linkedin\": \"\", \"portfolio\": \"\" },\n" +
                        "  \"summary\": \"\",\n" +
                        "  \"skills\": { \"technical\": [], \"soft\": [], \"tools\": [], \"languages\": [] },\n" +
                        "  \"experience\": [ { \"company\": \"\", \"position\": \"\", \"duration\": \"\", \"location\": \"\", \"responsibilities\": [], \"achievements\": [] } ],\n" +
                        "  \"education\": [ { \"institution\": \"\", \"degree\": \"\", \"graduation_year\": \"\", \"gpa\": \"\", \"location\": \"\" } ],\n" +
                        "  \"certifications\": [ { \"name\": \"\", \"issuer\": \"\", \"date\": \"\", \"expiry\": \"\" } ],\n" +
                        "  \"projects\": [ { \"name\": \"\", \"description\": \"\", \"technologies\": [], \"duration\": \"\" } ],\n" +
                        "  \"achievements\": [],\n" +
                        "  \"additional_info\": {\n" +
                        "    \"hobbies\": [],\n" +
                        "    \"volunteer\": [],\n" +
                        "    \"references\": \"\",\n" +
                        "    \"extra_info\": { \"any_other_fields_not_matching_above\": \"\" }\n" +
                        "  },\n" +
                        "  \"compatibility\": \"XX%%%%\",\n" +
                        "  \"criteria\": \"\"\n" +
                        "}\n\n" +
                        "🔸 Correct typos and normalize formatting (e.g., \"MySql\" \u2192 \"MySQL\").\n" +
                        "🔸 Map the information strictly to this structure. If any field doesn’t fit any above category, add it into the nested 'additional_info.extra_info' map.\n" +
                        "🔸 Only return the JSON strictly in the specified structure—no extra text or explanation.\n" +
                        "🔸 Be thorough and complete, extracting all available information from the resume PDF.\n\n" +
                        "Now also match this resume against the following job description:\n" +
                        "{\n" +
                        "  \"title\": \"%s\",\n" +
                        "  \"skills\": {\n" +
                        "    \"technical\": %s,\n" +
                        "    \"tools\": %s,\n" +
                        "    \"methodologies\": %s\n" +
                        "  }\n" +
                        "}\n\n" +
                        staticCriteria;

        // Sanitize inputs to escape % signs, to prevent format errors
        String safeTitle = jobDescription.map(JobDescription::getTitle).orElse("null").replace("%", "%%");
        String safeTechnical = jobDescription.map(jd -> {
            try {
                return objectMapper.writeValueAsString(
                        jd.getSkills() != null ? jd.getSkills().getTechnical() : List.of()
                ).replace("%", "%%");
            } catch (JsonProcessingException e) {
                return "[]";
            }
        }).orElse("[]");
        String safeTools = jobDescription.map(jd -> {
            try {
                return objectMapper.writeValueAsString(
                        jd.getSkills() != null ? jd.getSkills().getTools() : List.of()
                ).replace("%", "%%");
            } catch (JsonProcessingException e) {
                return "[]";
            }
        }).orElse("[]");
        String safeMethodologies = jobDescription.map(jd -> {
            try {
                return objectMapper.writeValueAsString(
                        jd.getSkills() != null ? jd.getSkills().getMethodologies() : List.of()
                ).replace("%", "%%");
            } catch (JsonProcessingException e) {
                return "[]";
            }
        }).orElse("[]");

        Map<String, Object> part1 = new HashMap<>();
        part1.put("text", String.format(
                textTemplate,
                safeTitle,
                safeTechnical,
                safeTools,
                safeMethodologies
        ));

        Map<String, Object> part2 = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", "application/pdf");
        inlineData.put("data", base64Pdf);
        part2.put("inline_data", inlineData);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part1, part2));

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(content));

        // Generation configuration
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("maxOutputTokens", 4000);
        payload.put("generationConfig", generationConfig);

        // Make the HTTP request to Gemini API
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

        String extractedJsonString = result.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        return extractedJsonString;
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

    public String extractResumeInfo(MultipartFile pdfFile, Long jd) {
        // Google Gemini first (FREE and excellent PDF support)
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                log.info("Using Google Gemini for PDF processing");
                return extractResumeInfoWithGemini(pdfFile , jd);
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

