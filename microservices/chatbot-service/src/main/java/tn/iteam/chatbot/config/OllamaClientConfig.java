package tn.iteam.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OllamaClientConfig {

    @Bean
    RestClient ollamaRestClient(@Value("${app.ollama.base-url}") String baseUrl) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return RestClient.builder().baseUrl(normalized).build();
    }
}
