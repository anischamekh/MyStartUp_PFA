package tn.iteam.common.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@SpringJUnitWebConfig(classes = {ApiSecuritySupportHeadersTest.TestApp.class, CommonSecurityAutoConfiguration.class})
class ApiSecuritySupportHeadersTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void securityHeaders_applied() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"));
    }

    @Configuration
    @EnableWebSecurity
    static class TestApp {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            ApiSecuritySupport.configureApiCsrf(http);
            ApiSecuritySupport.configureSecurityHeaders(http);
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }

        @RestController
        static class PingController {
            @GetMapping("/ping")
            String ping() {
                return "ok";
            }
        }
    }
}
