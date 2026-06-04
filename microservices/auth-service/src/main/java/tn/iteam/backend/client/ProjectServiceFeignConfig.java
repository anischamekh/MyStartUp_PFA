package tn.iteam.backend.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ProjectServiceFeignConfig {

    @Bean
    RequestInterceptor internalApiKeyInterceptor(@Value("${app.internal-api-key:}") String apiKey) {
        return template -> {
            if (apiKey != null && !apiKey.isBlank()) {
                template.header(InternalApiKeyHeaders.HEADER, apiKey);
            }
        };
    }
}
