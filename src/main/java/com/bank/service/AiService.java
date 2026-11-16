package com.bank.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;
    private static final String API_URL = "https://api.openai.com/v1/responses";
    private final String apiKey;

    public AiService(RestClient.Builder builder) {
        this.restClient = builder.build();

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String key = System.getenv("OPENAI_API_KEY");

        if (key == null || key.isBlank()) {
            key = dotenv.get("OPENAI_API_KEY");
        }

        this.apiKey = key;

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not configured"
            );
        }
    }

    public String ask(String question){
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
