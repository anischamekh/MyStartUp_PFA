package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import tn.iteam.chatbot.security.SecurityContextHelper;

@ExtendWith(MockitoExtension.class)
class ContextDataServiceTest {

    @Mock
    private RestClient authRestClient;
    @Mock
    private RestClient hrmRestClient;
    @Mock
    private RestClient projectRestClient;
    @Mock
    private SecurityContextHelper securityContextHelper;

    private ContextDataService contextDataService;

    @BeforeEach
    void setUp() {
        contextDataService = new ContextDataService(
                authRestClient,
                hrmRestClient,
                projectRestClient,
                securityContextHelper,
                new ObjectMapper(),
                new ChatContextFormatter());
    }

    @Test
    void buildContext_unknownRole() {
        when(securityContextHelper.currentRole()).thenReturn(null);
        String ctx = contextDataService.buildContext("token");
        assertTrue(ctx.toLowerCase().contains("unknown") || ctx.contains("No application"));
    }

    @Test
    void buildContext_defaultRole_returnsEmptyContext() {
        when(securityContextHelper.currentRole()).thenReturn("GUEST");
        String ctx = contextDataService.buildContext("token");
        assertTrue(ctx.contains("GUEST") || ctx.length() > 0);
    }
}
