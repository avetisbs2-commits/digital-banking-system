package com.bank.config;

import com.bank.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class AiConfiguration {

    @Bean
    public AiService aiService(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.api-key:}") String apiKey
    ) {
        RestClient restClient = apiKey == null || apiKey.isBlank()
                ? null
                : restClientBuilder.build();

        return new AiService(apiKey, restClient);
    }
}