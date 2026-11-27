package com.bank.service;

import com.bank.config.AiConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

class AiServiceTest {

    @Test
    void springConfigurationStartsWhenApiKeyIsMissing() {
        RestClient.Builder builder = RestClient.builder();
        contextRunner(builder, "")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertFalse(context.getBean(AiService.class).isAvailable());
                });
    }

    @Test
    void springConfigurationUsesEnvironmentPropertyAndBuildsClientWhenConfigured() {
        String testApiKey = "test-key";
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + testApiKey))
                .andExpect(content().json("{\"model\":\"gpt-6-luna\",\"input\":\"Test question\"}"))
                .andRespond(withSuccess(
                        "{\"output\":[{\"content\":[{\"type\":\"output_text\",\"text\":\"Test answer\"}]}]}",
                        MediaType.APPLICATION_JSON
                ));

        contextRunner(builder, testApiKey)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    AiService aiService = context.getBean(AiService.class);
                    assertTrue(aiService.isAvailable());
                    assertEquals("Test answer", aiService.ask("Test question"));
                });

        server.verify();
    }

    private ApplicationContextRunner contextRunner(RestClient.Builder builder, String key) {
        return new ApplicationContextRunner()
                .withUserConfiguration(AiConfiguration.class)
                .withBean(RestClient.Builder.class, () -> builder)
                .withInitializer(context -> TestPropertySourceUtils.addPropertiesFilesToEnvironment(
                        context,
                        "classpath:application.properties"
                ))
                .withPropertyValues("OPENAI_API_KEY=" + key);
    }
}