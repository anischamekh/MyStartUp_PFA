package tn.iteam.chatbot.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ServiceClientsConfig {

    @Bean
    public RestClient authRestClient(@Value("${app.services.auth-url}") String authUrl) {
        return RestClient.builder().baseUrl(authUrl).build();
    }

    @Bean
    public RestClient hrmRestClient(@Value("${app.services.hrm-url}") String hrmUrl) {
        return RestClient.builder().baseUrl(hrmUrl).build();
    }

    @Bean
    public RestClient projectRestClient(@Value("${app.services.project-url}") String projectUrl) {
        return RestClient.builder().baseUrl(projectUrl).build();
    }
}
