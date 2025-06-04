package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@org.springframework.stereotype.Service
public class Services {

    @Value("${openai.api.key}")
    private String openaiApiKey;
    private final String openAiUploadUrl = "https://api.openai.com/v1/files";
    private final String openAiChatUrl = "https://api.openai.com/v1/chat/completions";

    // Step 1: Upload the PDF file
    private String uploadFile(MultipartFile pdfFile) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(openAiUploadUrl);
            post.setHeader("Authorization", "Bearer " + openaiApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("purpose", "assistants");
            builder.addBinaryBody("file", pdfFile.getInputStream(),
                    ContentType.APPLICATION_PDF, pdfFile.getOriginalFilename());
            post.setEntity(builder.build());

            ClassicHttpResponse response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity());
            int status = response.getCode();
            if (status != 200) {
                throw new RuntimeException("File upload failed: " + body);
            }

            Map<String, Object> result = new ObjectMapper().readValue(body, Map.class);
            return (String) result.get("id");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // Step 2: Ask GPT to extract all information
    private String askAI(String fileId) throws IOException, InterruptedException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4.0-turbo"); // or gpt-3.5-turbo
        payload.put("temperature", 0.2);
        payload.put("max_tokens", 2000);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You are a resume parser. Extract all information from the attached resume PDF."),
                Map.of("role", "user", "content", "Please extract the candidate's name, contact details, skills, education, work experience, certifications, and achievements from the uploaded PDF."),
                Map.of("role", "user", "content", "file_id:" + fileId)
        );
        payload.put("messages", messages);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);

        var client = java.net.http.HttpClient.newHttpClient();
        var request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(openAiChatUrl))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (response.statusCode() != 200) {
            throw new RuntimeException("Chat completion failed: " + body);
        }

        Map<String, Object> result = mapper.readValue(body, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    // Main method: Upload + Extract
    public String extractResumeInfo(MultipartFile pdfFile) {
        try {
            String fileId = uploadFile(pdfFile);
            String extractedInfo = askAI(fileId);
            return extractedInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
