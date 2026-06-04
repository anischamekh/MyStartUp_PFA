package tn.iteam.backend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.iteam.backend.dto.LoginResponse;
import tn.iteam.backend.repository.AuditLogRepository;
import tn.iteam.backend.security.AuditLoggingFilter;
import tn.iteam.backend.security.JwtAuthenticationFilter;
import tn.iteam.backend.service.AuthService;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(tn.iteam.backend.config.OpenApiConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private AuditLoggingFilter auditLoggingFilter;
    @MockBean
    private AuditLogRepository auditLogRepository;

    @Test
    void login_returnsSessionWithoutTokensInBody() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(
                new LoginResponse("access-jwt", "refresh-jwt", 1L, "admin", "Admin User", "ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void logout_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
    }
}
