package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tn.iteam.chatbot.security.SecurityContextHelper;

@ExtendWith(MockitoExtension.class)
class ContextDataServiceIntegrationTest {

    private static final String AUTH = "http://auth";
    private static final String HRM = "http://hrm";
    private static final String PROJECT = "http://project";

    @Mock
    private SecurityContextHelper securityContextHelper;

    private MockRestServiceServer authServer;
    private MockRestServiceServer hrmServer;
    private MockRestServiceServer projectServer;
    private ContextDataService contextDataService;

    @BeforeEach
    void setUp() {
        RestClient.Builder authBuilder = RestClient.builder().baseUrl(AUTH);
        RestClient.Builder hrmBuilder = RestClient.builder().baseUrl(HRM);
        RestClient.Builder projectBuilder = RestClient.builder().baseUrl(PROJECT);
        authServer = MockRestServiceServer.bindTo(authBuilder).build();
        hrmServer = MockRestServiceServer.bindTo(hrmBuilder).build();
        projectServer = MockRestServiceServer.bindTo(projectBuilder).build();
        contextDataService = new ContextDataService(
                authBuilder.build(),
                hrmBuilder.build(),
                projectBuilder.build(),
                securityContextHelper,
                new ObjectMapper(),
                new ChatContextFormatter());
    }

    @AfterEach
    void resetServers() {
        if (authServer != null) {
            authServer.reset();
        }
        if (hrmServer != null) {
            hrmServer.reset();
        }
        if (projectServer != null) {
            projectServer.reset();
        }
    }

    @Test
    void buildContext_employeeRole_fetchesProfileAndHrData() {
        when(securityContextHelper.currentRole()).thenReturn("EMPLOYEE");
        stubJson(authServer, AUTH + "/api/users/me");
        stubJson(hrmServer, HRM + "/api/leaves/mine");
        stubJson(hrmServer, HRM + "/api/payroll");
        stubJson(projectServer, PROJECT + "/api/tasks/mine");
        stubJson(hrmServer, HRM + "/api/trainings");
        stubJson(hrmServer, HRM + "/api/documents");
        stubJson(hrmServer, HRM + "/api/notifications/mine");

        String ctx = contextDataService.buildContext("token");
        assertTrue(ctx.contains("EMPLOYEE") || ctx.contains("profile"));
        authServer.verify();
        hrmServer.verify();
        projectServer.verify();
    }

    @Test
    void buildContext_teamLeaderRole() {
        when(securityContextHelper.currentRole()).thenReturn("TEAM_LEADER");
        stubJson(authServer, AUTH + "/api/users/team-members");
        stubJson(projectServer, PROJECT + "/api/tasks");
        stubJson(hrmServer, HRM + "/api/leaves");
        stubJson(hrmServer, HRM + "/api/evaluations");

        String ctx = contextDataService.buildContext("token");
        assertTrue(ctx.length() > 10);
        authServer.verify();
    }

    @Test
    void buildContext_managerRole() {
        when(securityContextHelper.currentRole()).thenReturn("MANAGER");
        stubJson(projectServer, PROJECT + "/api/projects");
        stubJson(projectServer, PROJECT + "/api/tasks");
        stubJson(hrmServer, HRM + "/api/reports/summary");
        stubJson(hrmServer, HRM + "/api/leaves");

        assertTrue(contextDataService.buildContext("token").contains("MANAGER")
                || contextDataService.buildContext("token").length() > 5);
        projectServer.verify();
    }

    @Test
    void buildContext_hrRole() {
        when(securityContextHelper.currentRole()).thenReturn("HR");
        stubJson(authServer, AUTH + "/api/users/summaries");
        stubJson(hrmServer, HRM + "/api/leaves");
        stubJson(hrmServer, HRM + "/api/payroll");
        stubJson(hrmServer, HRM + "/api/trainings");
        stubJson(hrmServer, HRM + "/api/skills");
        stubJson(hrmServer, HRM + "/api/reports/summary");

        assertTrue(contextDataService.buildContext("token").length() > 5);
        authServer.verify();
    }

    @Test
    void buildContext_adminRole() {
        when(securityContextHelper.currentRole()).thenReturn("ADMIN");
        stubJson(authServer, AUTH + "/api/users/summaries");
        stubJson(authServer, AUTH + "/api/teams");
        stubJson(projectServer, PROJECT + "/api/projects");
        stubJson(projectServer, PROJECT + "/api/tasks");
        stubJson(hrmServer, HRM + "/api/reports/summary");
        stubJson(hrmServer, HRM + "/api/notifications/mine");

        assertTrue(contextDataService.buildContext("token").length() > 5);
        authServer.verify();
    }

    @Test
    void buildContext_fetchFailure_returnsUnavailableMarker() {
        when(securityContextHelper.currentRole()).thenReturn("MANAGER");
        RestClient.Builder hrmBuilder = RestClient.builder().baseUrl(HRM);
        MockRestServiceServer hrmMock = MockRestServiceServer.bindTo(hrmBuilder).build();
        RestClient.Builder projBuilder = RestClient.builder().baseUrl(PROJECT);
        MockRestServiceServer projMock = MockRestServiceServer.bindTo(projBuilder).build();
        ContextDataService service = new ContextDataService(
                RestClient.builder().baseUrl(AUTH).build(),
                hrmBuilder.build(),
                projBuilder.build(),
                securityContextHelper,
                new ObjectMapper(),
                new ChatContextFormatter());
        stubJson(projMock, PROJECT + "/api/projects");
        stubJson(projMock, PROJECT + "/api/tasks");
        hrmMock.expect(requestTo(HRM + "/api/reports/summary"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withServerError());
        hrmMock.expect(requestTo(HRM + "/api/leaves"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        String ctx = service.buildContext("token");
        assertTrue(ctx.contains("unavailable"));
    }

    private void stubJson(MockRestServiceServer server, String url) {
        server.expect(requestTo(url))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
    }
}
