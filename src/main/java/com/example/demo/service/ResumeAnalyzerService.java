//package com.example.demo.service;
//
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.net.http.HttpRequest.BodyPublisher;
//import java.net.http.HttpRequest.BodyPublishers;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.Random;
//import java.util.concurrent.Flow;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class ResumeAnalyzerService {
//
//    private static final String AI_API_URL = "http://ai-service/api/analyze";  // Replace with your AI endpoint
//
//    private final HttpClient httpClient;
//
//    public ResumeAnalyzerService() {
//        this.httpClient = HttpClient.newHttpClient();
//    }
//
//    public String analyze(MultipartFile resumeFile) throws IOException, InterruptedException {
//        if (resumeFile == null || resumeFile.isEmpty()) {
//            throw new IllegalArgumentException("Resume file is required");
//        }
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(AI_API_URL))
//                .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
//                .POST(ofMultipartData(resumeFile))
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            throw new RuntimeException("Failed to analyze resume: " + response.statusCode() + " - " + response.body());
//        }
//
//        return response.body();
//    }
//
//    // --- Multipart/form-data builder ---
//
//    private static final String BOUNDARY = "JavaBoundary" + new Random().nextInt(999999);
//
//    private static BodyPublisher ofMultipartData(MultipartFile file) throws IOException {
//        var byteArrays = new java.util.ArrayList<byte[]>();
//
//        // --boundary
//        // Content-Disposition: form-data; name="file"; filename="..."
//        // Content-Type: application/pdf
//        //
//        // ... file bytes ...
//        // --boundary--
//
//        String partHeader = "--" + BOUNDARY + "\r\n" +
//                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n" +
//                "Content-Type: " + file.getContentType() + "\r\n\r\n";
//        byteArrays.add(partHeader.getBytes(StandardCharsets.UTF_8));
//        byteArrays.add(file.getBytes());
//        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
//
//        String endMarker = "--" + BOUNDARY + "--\r\n";
//        byteArrays.add(endMarker.getBytes(StandardCharsets.UTF_8));
//
//        return BodyPublishers.ofByteArrays(byteArrays);
//    }
//}
//
//
//
//

package com.example.demo.service;

import com.example.demo.dto.AnalyzeResponse;
import com.example.demo.dto.ResumeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.*;

@Slf4j
@Service
public class ResumeAnalyzerService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    public ResumeResult analyze(String jobTitle, String jobDescription, List<MultipartFile> resumes) throws IOException, InterruptedException {
        List<ResumeResult> results = new ArrayList<>();

        for (MultipartFile resume : resumes) {
//            String rawText = extractTextFromPdf(resume);
//            if (rawText == null || rawText.isBlank()) {
//                log.warn("No text extracted from {}", resume.getOriginalFilename());
//                ResumeResult fallback = new ResumeResult();
//                fallback.setFileName(resume.getOriginalFilename());
//                fallback.setScore(0.0);
//                fallback.setSummary("Parsing error");
//                results.add(fallback);
//                continue;
//            }

//            String prompt = buildPrompt(jobTitle, jobDescription, rawText);
            return extractAllInfoFromPdf(resume);
//            ResumeResult parsed = parseResponse(gptResponse, resume.getOriginalFilename());
//            results.add(parsed);
        }

//        AnalyzeResponse response = new AnalyzeResponse();
//        response.setResults(results);
        return null;
    }

    private ResumeResult extractAllInfoFromPdf(MultipartFile pdfFile) {
        try {
            // Convert PDF to Base64 (since OpenRouter/OpenAI APIs usually accept text or multipart files)
            byte[] pdfBytes = pdfFile.getBytes();
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

            // Create the prompt
            String prompt = """
        You will receive a PDF resume. Extract all the following information from it:
        - Full name
        - Contact information (email, phone, address)
        - Summary or objective
        - Skills (list of skills or technologies)
        - Work experience (list of jobs with title, company, dates, responsibilities)
        - Education (list with degrees, institutions, dates)
        - Certifications (list)
        - Achievements or awards
        - Languages known
        - Hobbies or interests
        - Any other relevant information
        Please provide the response as a structured JSON with fields:
        {
          "name": "",
          "contact": { "email": "", "phone": "", "address": "" },
          "summary": "",
          "skills": [],
          "experience": [
            {"title": "", "company": "", "start_date": "", "end_date": "", "description": ""}
          ],
          "education": [
            {"degree": "", "institution": "", "start_date": "", "end_date": ""}
          ],
          "certifications": [],
          "achievements": [],
          "languages": [],
          "hobbies": []
        }
        Now, please analyze the PDF below.
        """;

            // Construct multipart/form-data with the prompt and the PDF
            String boundary = "Boundary-" + System.currentTimeMillis();
            String fileName = pdfFile.getOriginalFilename();

            String body = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"model\"\r\n\r\n" +
                    "gpt-4-vision-preview\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"prompt\"\r\n\r\n" +
                    prompt + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                    "Content-Type: " + pdfFile.getContentType() + "\r\n\r\n" +
                    new String(pdfBytes) + "\r\n" +
                    "--" + boundary + "--\r\n";

            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("OpenRouter error: {}", response.body());
                throw new RuntimeException("PDF extraction failed");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            String gptResponse = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

            // Extract JSON from GPT response and parse into ResumeResult
            String extractedJson = extractJson(gptResponse);
            ResumeResult resumeResult = mapper.readValue(extractedJson, ResumeResult.class);
            resumeResult.setFileName(pdfFile.getOriginalFilename());
            return resumeResult;

        } catch (Exception e) {
            log.error("Extraction failed: {}", e.getMessage());
            ResumeResult fallback = new ResumeResult();
            fallback.setFileName(pdfFile.getOriginalFilename());
            fallback.setScore(0.0);
            fallback.setSummary("Error extracting data from PDF");
            return fallback;
        }
    }

    // Helper to extract JSON from GPT response
//    private String extractJson(String text) {
//        int start = text.indexOf("{");
//        int end = text.lastIndexOf("}");
//        if (start >= 0 && end >= 0) {
//            return text.substring(start, end + 1);
//        }
//        return null;
//    }


//    private String uploadFile(MultipartFile prompt) throws IOException, InterruptedException {
//        String boundary = "Boundary-" + System.currentTimeMillis();
//
//        var client = java.net.http.HttpClient.newHttpClient();
//
//        String fileContent = new String(prompt.getBytes());
//        String fileName = prompt.getOriginalFilename();
//
//        String body = "--" + boundary + "\r\n" +
//                "Content-Disposition: form-data; name=\"purpose\"\r\n\r\n" +
//                "assistants\r\n" +   // Or fine-tune, depending on usage
//                "--" + boundary + "\r\n" +
//                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
//                "Content-Type: " + prompt.getContentType() + "\r\n\r\n" +
//                fileContent + "\r\n" +
//                "--" + boundary + "--\r\n";
//
//        var request = java.net.http.HttpRequest.newBuilder()
//                .uri(URI.create("https://api.openai.com/v1/files"))  // Or OpenRouter equivalent
//                .header("Authorization", "Bearer " + openaiApiKey)
//                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
//                .POST(HttpRequest.BodyPublishers.ofString(body))
//                .build();
//
//        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() != 200) {
//            log.error("File upload error: {}", response.body());
//            throw new RuntimeException("File upload failed");
//        }
//
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, Object> result = mapper.readValue(response.body(), Map.class);
//        return (String) result.get("id");  // file_id
//    }


    private String extractTextFromPdf(MultipartFile file) {
        File tempFile = null;
        try {
            tempFile = convertMultipartToFile(file);
            try (PDDocument document = PDDocument.load(tempFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e) {
            log.error("PDF text extraction failed for {}: {}", file.getOriginalFilename(), e.getMessage());
            return "";
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private File convertMultipartToFile(MultipartFile file) throws IOException {
        String suffix = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".tmp");
        File convFile = File.createTempFile("resume_", suffix);
        file.transferTo(convFile);
        return convFile;
    }

    private String buildPrompt(String jobTitle, String jobDesc, String resumeText) {
        return """
        You are a smart resume analyzer.
        Compare the resume below with the job description and return a detailed JSON response:
        {
          "name": "Candidate Name",
          "skills": ["Skill1", "Skill2", "..."],
          "experience": ["Company1: Role, Duration", "..."],
          "education": ["Degree, Institution, Year", "..."],
          "achievements": ["Achievement1", "..."],
          "score": 0-10,
          "summary": "Concise summary"
        }

        --- JOB TITLE ---
        %s

        --- JOB DESCRIPTION ---
        %s

        --- RESUME TEXT ---
        %s
        """.formatted(jobTitle, jobDesc, resumeText);
    }

    private String callOpenAi(String prompt) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "openai/gpt-3.5-turbo");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            payload.put("messages", messages);
            payload.put("max_tokens", 1000);
            payload.put("temperature", 0.2);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);

            var client = java.net.http.HttpClient.newHttpClient();
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("OpenRouter error: {}", response.body());
                return "";
            }

            Map<String, Object> result = mapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            log.error("OpenRouter call failed: {}", e.getMessage());
            return "";
        }
    }

    private ResumeResult parseResponse(String gptResponse, String fileName) {
        try {
            String extractedJson = extractJson(gptResponse);
            if (extractedJson == null) throw new RuntimeException("No valid JSON");

            ObjectMapper mapper = new ObjectMapper();
            ResumeResult result = mapper.readValue(extractedJson, ResumeResult.class);
            result.setFileName(fileName);
            return result;
        } catch (Exception e) {
            log.error("Parsing failed: {}", e.getMessage());
            ResumeResult fallback = new ResumeResult();
            fallback.setFileName(fileName);
            fallback.setScore(0.0);
            fallback.setSummary("Parsing error");
            return fallback;
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }
}




//Done

//package com.example.demo.service;
//
//import com.example.demo.dto.AnalyzeResponse;
//import com.example.demo.dto.ResumeResult;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.tika.Tika;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.InputStream;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.*;
//
//@Slf4j
//@Service
//public class ResumeAnalyzerService {
//
//    @Value("${openai.api.key}")
//    private String openaiApiKey;
//
//    private static final Tika tika = new Tika();
//
//    public AnalyzeResponse analyze(String jobTitle, String jobDescription, List<MultipartFile> resumes) {
//        List<ResumeResult> results = new ArrayList<>();
//
//        for (MultipartFile resume : resumes) {
//            String rawText = extractText(resume);
//            String cleanedText = preCleanResumeText(rawText);
//            String prompt = buildEnhancedPrompt(jobTitle, jobDescription, cleanedText);
//            String gptResponse = callOpenAi(prompt);
//            ResumeResult parsed = parseEnhancedResponse(gptResponse, resume.getOriginalFilename());
//            results.add(parsed);
//        }
//
//        AnalyzeResponse response = new AnalyzeResponse();
//        response.setResults(results);
//        return response;
//    }
//
//    private String extractText(MultipartFile file) {
//        try (InputStream stream = file.getInputStream()) {
//            return tika.parseToString(stream);
//        } catch (Exception e) {
//            log.error("Text extraction failed for file {}: {}", file.getOriginalFilename(), e.getMessage());
//            return "";
//        }
//    }
//
//    private String preCleanResumeText(String text) {
//        if (text == null || text.isBlank()) return "";
//        return text
//                .replaceAll("\\r?\\n", "\n")
//                .replaceAll("[ \\t]+", " ")
//                .replaceAll("\\n{2,}", "\n\n")
//                .replaceAll("(?m)^\\s*-", "-")
//                .replaceAll("(?m)^\\s+", "")
//                .trim();
//    }
//
//    private String buildEnhancedPrompt(String jobTitle, String jobDesc, String resumeText) {
//        return """
//        You are an expert resume analyzer.
//        Compare the following resume with the given job description and RETURN ONLY JSON. Format:
//
//        {
//          "name": "Full Candidate Name",
//          "skills": ["Skill1", "Skill2", "..."],
//          "experience": [
//            {"company": "Company A", "role": "Role A", "years": "X years"},
//            {"company": "Company B", "role": "Role B", "years": "Y years"}
//          ],
//          "score": 0-10,
//          "summary": "Short summary of candidate"
//        }
//
//        --- JOB TITLE ---
//        %s
//
//        --- JOB DESCRIPTION ---
//        %s
//
//        --- RESUME TEXT ---
//        %s
//
//        -----------------------
//        List all skills and full experience in JSON as specified.
//        """.formatted(jobTitle, jobDesc, resumeText);
//    }
//
//    private String callOpenAi(String prompt) {
//        try {
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("model", "openai/gpt-3.5-turbo");
//
//            Map<String, String> message = new HashMap<>();
//            message.put("role", "user");
//            message.put("content", prompt);
//
//            payload.put("messages", List.of(message));
//            payload.put("temperature", 0.2);
//
//            ObjectMapper mapper = new ObjectMapper();
//            String json = mapper.writeValueAsString(payload);
//
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
//                    .header("Authorization", "Bearer " + openaiApiKey)
//                    .header("Content-Type", "application/json")
//                    .header("HTTP-Referer", "http://localhost")
//                    .header("X-Title", "ResumeAnalyzerHackathon2025")
//                    .POST(HttpRequest.BodyPublishers.ofString(json))
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() != 200) {
//                log.error("OpenRouter API error: {}", response.body());
//                return "{}";
//            }
//
//            Map<String, Object> fullResponse = mapper.readValue(response.body(), Map.class);
//            List<Map<String, Object>> choices = (List<Map<String, Object>>) fullResponse.get("choices");
//            Map<String, Object> firstChoice = choices.get(0);
//            Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
//
//            return (String) messageMap.get("content");
//
//        } catch (Exception e) {
//            log.error("OpenRouter API call failed: {}", e.getMessage());
//            return "{}";
//        }
//    }
//
//    private ResumeResult parseEnhancedResponse(String gptResponse, String fileName) {
//        try {
//            log.debug("Raw GPT response for {}: {}", fileName, gptResponse);
//
//            String json = extractJson(gptResponse);
//            if (json == null) throw new RuntimeException("No valid JSON found in GPT response");
//
//            ObjectMapper mapper = new ObjectMapper();
//            ResumeResult result = mapper.readValue(json, ResumeResult.class);
//            result.setFileName(fileName);
//            return result;
//
//        } catch (Exception e) {
//            log.error("Parsing failed for {}: {}", fileName, e.getMessage());
//            ResumeResult fallback = new ResumeResult();
//            fallback.setFileName(fileName);
//            fallback.setName("Unknown");
//            fallback.setSkills(Collections.emptyList());
//            fallback.setExperience(Collections.emptyList());
//            fallback.setScore(0.0);
//            fallback.setSummary("Parsing failed");
//            return fallback;
//        }
//    }
//
//    private String extractJson(String gptResponse) {
//        int start = gptResponse.indexOf('{');
//        int end = gptResponse.lastIndexOf('}');
//        if (start != -1 && end != -1 && end > start) {
//            return gptResponse.substring(start, end + 1);
//        } else {
//            return null;
//        }
//    }
//}
//
//


//package com.example.demo.service;
//
//import com.example.demo.dto.AnalyzeRequest;
//import com.example.demo.dto.AnalyzeResponse;
//import com.example.demo.dto.ResumeRequest;
//import com.example.demo.dto.ResumeResult;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.tika.Tika;
//import java.io.InputStream;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Map;
//
//import org.springframework.web.multipart.MultipartFile;
//
//@Slf4j
//@Service
//public class ResumeAnalyzerService {
//
//    @Value("${openai.api.key}")
//    private String openaiApiKey;
//
//    private static final Tika tika = new Tika();
//
//    public AnalyzeResponse analyze(String jobTitle, String jobDescription, List<MultipartFile> resumes) {
//        List<ResumeResult> results = new ArrayList<>();
//
//        for (MultipartFile resume : resumes) {
//            String rawText = extractText(resume);
//            String cleanedText = preCleanResumeText(rawText);
//            String prompt = buildPrompt(jobTitle, jobDescription, cleanedText);
//            String gptResponse = callOpenAi(prompt,resume);
//            ResumeResult parsed = parseResponse(gptResponse, resume.getOriginalFilename());
//            results.add(parsed);
//        }
//
//        AnalyzeResponse response = new AnalyzeResponse();
//        response.setResults(results);
//        return response;
//    }
//
//    private String extractText(MultipartFile file) {
//        try (InputStream stream = file.getInputStream()) {
//            return tika.parseToString(stream);
//        } catch (Exception e) {
//            log.error("Text extraction failed for file {}: {}", file.getOriginalFilename(), e.getMessage());
//            return "";
//        }
//    }
//
//    private String preCleanResumeText(String text) {
//        if (text == null || text.isBlank()) return "";
//
//        return text
//                .replaceAll("\\r?\\n", "\n")
//                .replaceAll("[ \\t]+", " ")
//                .replaceAll("\\n{2,}", "\n\n")
//                .replaceAll("(?m)^\\s*-", "-")
//                .replaceAll("(?m)^\\s+", "")
//                .trim();
//    }
//
//    private String buildPrompt(String jobTitle, String jobDesc, String resumeText) {
//        return """
//            You are an intelligent resume evaluator.
//            Compare the following resume with the given job description and RETURN ONLY VALID JSON.
//            Strict JSON format, no explanations. Format:
//            {
//              "name": "Candidate Name",
//              "skills": ["Skill1", "Skill2"],
//              "score": 0-10,
//              "summary": "Short summary"
//            }
//
//            --- JOB TITLE ---
//            %s
//
//            --- JOB DESCRIPTION ---
//            %s
//
//            --- RESUME TEXT ---
//            %s
//
//            -----------------------
//            In the end List all the skills he has experience.list them properly
//            """.formatted(jobTitle, jobDesc, resumeText);
//    }
//
//    private String callOpenAi(String prompt, MultipartFile resume) {
//        try {
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("model", "openai/gpt-3.5-turbo");
//
//            Map<String, String> message = new HashMap<>();
//            message.put("role", "user");
//            message.put("content", prompt);
//
//            payload.put("messages", List.of(message));
//            payload.put("temperature", 0.2);
//
//            ObjectMapper mapper = new ObjectMapper();
//            String json = mapper.writeValueAsString(payload);
//
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
//                    .header("Authorization", "Bearer " + openaiApiKey)
//                    .header("Content-Type", "application/json")
//                    .header("HTTP-Referer", "http://localhost")
//                    .header("X-Title", "ResumeAnalyzerApp")
//                    .POST(HttpRequest.BodyPublishers.ofString(json))
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() != 200) {
//                log.error("OpenRouter API error: {}", response.body());
//                return "OpenRouter API error: " + response.body();
//            }
//
//            Map<String, Object> fullResponse = mapper.readValue(response.body(), Map.class);
//            List<Map<String, Object>> choices = (List<Map<String, Object>>) fullResponse.get("choices");
//            Map<String, Object> firstChoice = choices.get(0);
//            Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
//
//            return (String) messageMap.get("content");
//
//        } catch (Exception e) {
//            log.error("OpenRouter API call failed: {}", e.getMessage());
//            return "API call failed: " + e.getMessage();
//        }
//    }
//
//    private ResumeResult parseResponse(String gptResponse, String fileName) {
//        try {
//            log.debug("Raw GPT response for {}: {}", fileName, gptResponse);
//
//            String extractedJson = extractJson(gptResponse);
//            String cleanedJson = cleanJson(extractedJson);
//
//            if (cleanedJson == null) {
//                throw new RuntimeException("No valid JSON found in GPT response");
//            }
//
//            ObjectMapper mapper = new ObjectMapper();
//            ResumeResult result = mapper.readValue(cleanedJson, ResumeResult.class);
//            result.setFileName(fileName);
//            return result;
//
//        } catch (Exception e) {
//            log.error("Failed to parse GPT response for {}: {}", fileName, e.getMessage());
//            ResumeResult fallback = new ResumeResult();
//            fallback.setFileName(fileName);
//            fallback.setScore(0.0);
//            fallback.setSummary("Parsing failed");
//            return fallback;
//        }
//    }
//
//    private String extractJson(String gptResponse) {
//        int start = gptResponse.indexOf('{');
//        int end = gptResponse.lastIndexOf('}');
//        if (start != -1 && end != -1 && end > start) {
//            return gptResponse.substring(start, end + 1);
//        } else {
//            return null;
//        }
//    }
//
//    private String cleanJson(String json) {
//        if (json == null) return null;
//        return json
//                .replaceAll("(?m)^\\s*-\\s*", "")  // Remove dashes at line start
//                .replaceAll(",\\s*}", "}")         // Remove trailing commas before }
//                .replaceAll(",\\s*]", "]")         // Remove trailing commas before ]
//                .trim();
//    }
//}