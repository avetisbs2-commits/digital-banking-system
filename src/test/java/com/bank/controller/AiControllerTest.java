package com.bank.controller;

import com.bank.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiControllerTest {

    @Test
    void returnsServiceUnavailableWhenApiKeyIsMissing() throws Exception {
        AiService aiService = new AiService(" ", null);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AiController(aiService)).build();

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Explain currency conversion\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("AI assistant is not configured. Set OPENAI_API_KEY to enable it."));
    }

    @Test
    void sendsRequestAndReturnsAnswerWhenApiKeyIsConfigured() throws Exception {
        String testApiKey = "test-key";
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + testApiKey))
                .andExpect(content().json("{\"model\":\"gpt-6-luna\",\"input\":\"Explain currency conversion\"}"))
                .andRespond(withSuccess(
                        "{\"output\":[{\"content\":[{\"type\":\"output_text\",\"text\":\"Currency conversion answer\"}]}]}",
                        MediaType.APPLICATION_JSON
                ));

        AiService aiService = new AiService(testApiKey, builder.build());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AiController(aiService)).build();

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Explain currency conversion\"}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("Currency conversion answer"));

        server.verify();
    }
}
