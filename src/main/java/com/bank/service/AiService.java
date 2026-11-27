package com.bank.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Map;

public class AiService {

    private static final String API_URL = "https://api.openai.com/v1/responses";

    private final String apiKey;
    private final RestClient restClient;

    public AiService(String apiKey, RestClient restClient) {
        this.apiKey = apiKey;
        this.restClient = restClient;
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String ask(String question) {
        if (!isAvailable()) {
            throw new IllegalStateException("AI assistant is not configured.");
        }

        JsonNode responseJson = restClient.post()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "model", "gpt-6-luna",
                        "input", question
                ))
                .retrieve()
                .body(JsonNode.class);

        for (JsonNode output : responseJson.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    return content.path("text").asText();
                }
            }
        }

        throw new IllegalStateException("OpenAI returned no text");
    }
}