package tn.iteam.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tn.iteam.chatbot.security.SecurityContextHelper;

@Service
public class ContextDataService {

    private static final Logger log = LoggerFactory.getLogger(ContextDataService.class);

    private final RestClient authRestClient;
    private final RestClient hrmRestClient;
    private final RestClient projectRestClient;
    private final SecurityContextHelper securityContextHelper;
    private final ObjectMapper objectMapper;
    private final ChatContextFormatter contextFormatter;

    public ContextDataService(
            @Qualifier("authRestClient") RestClient authRestClient,
            @Qualifier("hrmRestClient") RestClient hrmRestClient,
            @Qualifier("projectRestClient") RestClient projectRestClient,
            SecurityContextHelper securityContextHelper,
            ObjectMapper objectMapper,
            ChatContextFormatter contextFormatter
    ) {
        this.authRestClient = authRestClient;
        this.hrmRestClient = hrmRestClient;
        this.projectRestClient = projectRestClient;
        this.securityContextHelper = securityContextHelper;
        this.objectMapper = objectMapper;
        this.contextFormatter = contextFormatter;
    }

    public String buildContext(String bearerToken) {
        String role = securityContextHelper.currentRole();
        if (role == null) {
            return "No application data available (unknown role).";
        }
        ObjectNode data = switch (role) {
            case "EMPLOYEE" -> employeeData(bearerToken);
            case "TEAM_LEADER" -> teamLeaderData(bearerToken);
            case "MANAGER" -> managerData(bearerToken);
            case "HR" -> hrData(bearerToken);
            case "ADMIN" -> adminData(bearerToken);
            default -> objectMapper.createObjectNode();
        };
        return contextFormatter.format(role, data);
    }

    private ObjectNode employeeData(String token) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("profile", fetchNode(token, authRestClient, "/api/users/me"));
        root.set("myLeaves", fetchNode(token, hrmRestClient, "/api/leaves/mine"));
        root.set("myPayroll", fetchNode(token, hrmRestClient, "/api/payroll"));
        root.set("myTasks", fetchNode(token, projectRestClient, "/api/tasks/mine"));
        root.set("myTrainings", fetchNode(token, hrmRestClient, "/api/trainings"));
        root.set("myDocuments", fetchNode(token, hrmRestClient, "/api/documents"));
        root.set("myNotifications", fetchNode(token, hrmRestClient, "/api/notifications/mine"));
        return root;
    }

    private ObjectNode teamLeaderData(String token) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("teamMembers", fetchNode(token, authRestClient, "/api/users/team-members"));
        root.set("tasks", fetchNode(token, projectRestClient, "/api/tasks"));
        root.set("leaves", fetchNode(token, hrmRestClient, "/api/leaves"));
        root.set("evaluations", fetchNode(token, hrmRestClient, "/api/evaluations"));
        return root;
    }

    private ObjectNode managerData(String token) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("projects", fetchNode(token, projectRestClient, "/api/projects"));
        root.set("tasks", fetchNode(token, projectRestClient, "/api/tasks"));
        root.set("reports", fetchNode(token, hrmRestClient, "/api/reports/summary"));
        root.set("leaves", fetchNode(token, hrmRestClient, "/api/leaves"));
        return root;
    }

    private ObjectNode hrData(String token) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("users", fetchNode(token, authRestClient, "/api/users/summaries"));
        root.set("leaves", fetchNode(token, hrmRestClient, "/api/leaves"));
        root.set("payroll", fetchNode(token, hrmRestClient, "/api/payroll"));
        root.set("trainings", fetchNode(token, hrmRestClient, "/api/trainings"));
        root.set("skills", fetchNode(token, hrmRestClient, "/api/skills"));
        root.set("reports", fetchNode(token, hrmRestClient, "/api/reports/summary"));
        return root;
    }

    private ObjectNode adminData(String token) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("users", fetchNode(token, authRestClient, "/api/users/summaries"));
        root.set("teams", fetchNode(token, authRestClient, "/api/teams"));
        root.set("projects", fetchNode(token, projectRestClient, "/api/projects"));
        root.set("tasks", fetchNode(token, projectRestClient, "/api/tasks"));
        root.set("reports", fetchNode(token, hrmRestClient, "/api/reports/summary"));
        root.set("notifications", fetchNode(token, hrmRestClient, "/api/notifications/mine"));
        return root;
    }

    private JsonNode fetchNode(String token, RestClient client, String path) {
        try {
            JsonNode node = client.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(JsonNode.class);
            if (node == null) {
                log.warn("Empty response from {}", path);
                return objectMapper.createArrayNode();
            }
            return node;
        } catch (RestClientException ex) {
            log.warn("Failed to fetch {}: {}", path, ex.getMessage());
            return objectMapper.getNodeFactory().textNode("unavailable:" + path);
        }
    }
}
