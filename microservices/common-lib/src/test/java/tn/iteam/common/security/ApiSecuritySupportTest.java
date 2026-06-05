package tn.iteam.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;

@SpringJUnitWebConfig(classes = {ApiSecuritySupportTest.TestApp.class, CommonSecurityAutoConfiguration.class})
class ApiSecuritySupportTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void corsConfigurationSource_usesProperties() {
        ApiCorsProperties props = new ApiCorsProperties();
        props.setAllowedOrigins(List.of("https://app.example"));
        CorsConfiguration cfg = ApiSecuritySupport.corsConfigurationSource(props)
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/x"));
        assertTrue(cfg.getAllowedOrigins().contains("https://app.example"));
        assertFalse(cfg.getAllowedHeaders().contains("*"));
    }

    @Test
    void postWithoutCsrf_isForbidden_exceptBootstrapAuth() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mockMvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void postWithCsrf_isAllowed() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mockMvc.perform(post("/api/items").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void getDoesNotRequireCsrf() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mockMvc.perform(get("/api/items")).andExpect(status().isOk());
    }

    @Configuration
    @EnableWebSecurity
    static class TestApp {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            ApiSecuritySupport.configureApiCsrf(http);
            ApiSecuritySupport.applyCors(http, false);
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }

        @RestController
        static class TestController {
            @PostMapping("/api/items")
            String create() {
                return "ok";
            }

            @PostMapping("/api/auth/login")
            String login() {
                return "ok";
            }

            @GetMapping("/api/items")
            String list() {
                return "ok";
            }
        }
    }
}
