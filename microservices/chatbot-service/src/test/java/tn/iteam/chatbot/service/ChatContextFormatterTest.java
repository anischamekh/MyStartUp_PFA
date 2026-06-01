package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class ChatContextFormatterTest {

    private final ChatContextFormatter formatter = new ChatContextFormatter();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void formatAdmin_statesZeroProjectsClearly() throws Exception {
        ObjectNode data = mapper.createObjectNode();
        data.set("users", mapper.readTree("""
                [{"id":1,"username":"admin","fullName":"Admin","role":"ADMIN"}]
                """));
        data.set("teams", mapper.createArrayNode());
        data.set("projects", mapper.createArrayNode());
        data.set("tasks", mapper.createArrayNode());
        data.set("reports", mapper.readTree("""
                {"employeesByTeam":{"Frontend":2},"leavesByStatus":{},"tasksByStatus":{}}
                """));
        data.set("notifications", mapper.createArrayNode());

        String text = formatter.format("ADMIN", data);

        assertTrue(text.contains("Projects: 0 total, 0 ACTIVE"));
        assertTrue(text.contains("Users (1 total)"));
        assertTrue(text.contains("Employees per team"));
    }

    @Test
    void formatAdmin_countsActiveProjects() throws Exception {
        ObjectNode data = mapper.createObjectNode();
        ArrayNode projects = mapper.createArrayNode();
        ObjectNode p1 = mapper.createObjectNode();
        p1.put("name", "Portal");
        p1.put("status", "ACTIVE");
        p1.put("progress", 40);
        projects.add(p1);
        ObjectNode p2 = mapper.createObjectNode();
        p2.put("name", "Legacy");
        p2.put("status", "COMPLETED");
        p2.put("progress", 100);
        projects.add(p2);
        data.set("projects", projects);
        data.set("users", mapper.createArrayNode());
        data.set("teams", mapper.createArrayNode());
        data.set("tasks", mapper.createArrayNode());
        data.set("reports", mapper.createObjectNode());
        data.set("notifications", mapper.createArrayNode());

        String text = formatter.format("ADMIN", data);

        assertTrue(text.contains("2 total, 1 ACTIVE"));
        assertTrue(text.contains("Portal"));
    }
}
