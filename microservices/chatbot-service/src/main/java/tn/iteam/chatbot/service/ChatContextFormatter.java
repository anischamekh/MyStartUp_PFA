package tn.iteam.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;

/**
 * Turns fetched API JSON into short, factual text for the LLM (avoids raw JSON echoing).
 */
@Component
public class ChatContextFormatter {

    private static final int MAX_LIST_ITEMS = 15;

    public String format(String role, ObjectNode data) {
        if (role == null || data == null) {
            return "No application data available for your role.";
        }
        return switch (role) {
            case "EMPLOYEE" -> formatEmployee(data);
            case "TEAM_LEADER" -> formatTeamLeader(data);
            case "MANAGER" -> formatManager(data);
            case "HR" -> formatHr(data);
            case "ADMIN" -> formatAdmin(data);
            default -> "No application data available for role " + role + ".";
        };
    }

    private String formatAdmin(ObjectNode data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ADMIN (read-only system view)\n\n");
        appendUsers(sb, data.get("users"), "Users");
        appendTeams(sb, data.get("teams"));
        appendProjects(sb, data.get("projects"));
        appendTasks(sb, data.get("tasks"));
        appendReports(sb, data.get("reports"));
        appendNotifications(sb, data.get("notifications"));
        return sb.toString().trim();
    }

    private String formatHr(ObjectNode data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: HR\n\n");
        appendUsers(sb, data.get("users"), "Employees");
        appendSection(sb, "Leave requests", summarizeArray(data.get("leaves"), "id", "status", "userId"));
        appendSection(sb, "Payroll records", countLabel(data.get("payroll"), "entries"));
        appendSection(sb, "Trainings", countLabel(data.get("trainings"), "trainings"));
        appendSection(sb, "Skills", countLabel(data.get("skills"), "skills"));
        appendReports(sb, data.get("reports"));
        return sb.toString().trim();
    }

    private String formatManager(ObjectNode data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: MANAGER\n\n");
        appendProjects(sb, data.get("projects"));
        appendTasks(sb, data.get("tasks"));
        appendReports(sb, data.get("reports"));
        appendSection(sb, "Leave requests", summarizeArray(data.get("leaves"), "id", "status"));
        return sb.toString().trim();
    }

    private String formatTeamLeader(ObjectNode data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: TEAM_LEADER\n\n");
        appendUsers(sb, data.get("teamMembers"), "Team members");
        appendTasks(sb, data.get("tasks"));
        appendSection(sb, "Leave requests", summarizeArray(data.get("leaves"), "id", "status"));
        appendSection(sb, "Evaluations", countLabel(data.get("evaluations"), "evaluations"));
        return sb.toString().trim();
    }

    private String formatEmployee(ObjectNode data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: EMPLOYEE\n\n");
        appendProfile(sb, data.get("profile"));
        appendSection(sb, "My leave requests", summarizeArray(data.get("myLeaves"), "id", "status", "startDate", "endDate"));
        appendSection(sb, "My payroll", countLabel(data.get("myPayroll"), "payroll records"));
        appendTasks(sb, data.get("myTasks"));
        appendSection(sb, "Trainings", countLabel(data.get("myTrainings"), "trainings"));
        appendSection(sb, "Documents", countLabel(data.get("myDocuments"), "documents"));
        appendNotifications(sb, data.get("myNotifications"));
        return sb.toString().trim();
    }

    private void appendProfile(StringBuilder sb, JsonNode profile) {
        if (unavailable(profile)) {
            sb.append("Profile: unavailable\n");
            return;
        }
        if (profile == null || profile.isNull()) {
            sb.append("Profile: not loaded\n");
            return;
        }
        sb.append("Profile: ")
                .append(text(profile, "fullName", "username"))
                .append(" (")
                .append(text(profile, "username", ""))
                .append("), role ")
                .append(roleName(profile))
                .append("\n");
        JsonNode ep = profile.get("employeeProfile");
        if (ep != null && ep.isObject()) {
            sb.append("  Job: ").append(text(ep, "jobTitle", "n/a"));
            sb.append(", remaining leave days: ").append(ep.path("remainingLeaveDays").asInt(0));
            sb.append("\n");
        }
    }

    private void appendUsers(StringBuilder sb, JsonNode users, String label) {
        if (unavailable(users)) {
            sb.append(label).append(": unavailable\n");
            return;
        }
        if (!users.isArray()) {
            sb.append(label).append(": no data\n");
            return;
        }
        int total = users.size();
        sb.append(label).append(" (").append(total).append(" total):\n");
        if (total == 0) {
            sb.append("  - none\n");
            return;
        }
        int shown = 0;
        for (JsonNode u : users) {
            if (shown >= MAX_LIST_ITEMS) {
                sb.append("  - ... and ").append(total - MAX_LIST_ITEMS).append(" more\n");
                break;
            }
            String name = text(u, "fullName", "username");
            String role = u.has("role") && u.get("role").isTextual()
                    ? u.get("role").asText()
                    : roleName(u);
            String team = text(u, "teamName", "");
            sb.append("  - ").append(name).append(" (").append(role);
            if (!team.isBlank()) {
                sb.append(", team ").append(team);
            }
            sb.append(")\n");
            shown++;
        }
        Map<String, Long> byRole = countByField(users, "role", true);
        if (!byRole.isEmpty()) {
            sb.append("  By role: ").append(formatCounts(byRole)).append("\n");
        }
    }

    private void appendTeams(StringBuilder sb, JsonNode teams) {
        if (unavailable(teams)) {
            sb.append("Teams: unavailable\n");
            return;
        }
        if (!teams.isArray()) {
            sb.append("Teams: no data\n");
            return;
        }
        sb.append("Teams (").append(teams.size()).append(" total):\n");
        if (teams.isEmpty()) {
            sb.append("  - none\n");
            return;
        }
        for (JsonNode t : teams) {
            sb.append("  - ").append(text(t, "name", "Team"))
                    .append(" (").append(text(t, "speciality", "speciality n/a")).append(")\n");
        }
    }

    private void appendProjects(StringBuilder sb, JsonNode projects) {
        if (unavailable(projects)) {
            sb.append("Projects: unavailable\n");
            return;
        }
        if (!projects.isArray()) {
            sb.append("Projects: no data\n");
            return;
        }
        int total = projects.size();
        long active = countWhere(projects, "status", "ACTIVE");
        sb.append("Projects: ").append(total).append(" total, ").append(active).append(" ACTIVE\n");
        if (total == 0) {
            sb.append("  - no projects in the database\n");
            return;
        }
        Map<String, Long> byStatus = countByField(projects, "status", false);
        sb.append("  By status: ").append(formatCounts(byStatus)).append("\n");
        int shown = 0;
        for (JsonNode p : projects) {
            if (shown >= MAX_LIST_ITEMS) {
                break;
            }
            sb.append("  - ")
                    .append(text(p, "name", "Project"))
                    .append(" [")
                    .append(text(p, "status", "UNKNOWN"))
                    .append(", progress ")
                    .append(p.path("progress").asInt(0))
                    .append("%]\n");
            shown++;
        }
    }

    private void appendTasks(StringBuilder sb, JsonNode tasks) {
        if (unavailable(tasks)) {
            sb.append("Tasks: unavailable\n");
            return;
        }
        if (!tasks.isArray()) {
            sb.append("Tasks: no data\n");
            return;
        }
        int total = tasks.size();
        sb.append("Tasks: ").append(total).append(" total\n");
        if (total == 0) {
            sb.append("  - none\n");
            return;
        }
        Map<String, Long> byStatus = countByField(tasks, "status", false);
        sb.append("  By status: ").append(formatCounts(byStatus)).append("\n");
        int shown = 0;
        for (JsonNode t : tasks) {
            if (shown >= 8) {
                break;
            }
            sb.append("  - ")
                    .append(text(t, "title", "Task"))
                    .append(" [")
                    .append(text(t, "status", "UNKNOWN"))
                    .append("]\n");
            shown++;
        }
    }

    private void appendReports(StringBuilder sb, JsonNode reports) {
        if (unavailable(reports)) {
            sb.append("HR reports: unavailable\n");
            return;
        }
        if (reports == null || !reports.isObject()) {
            sb.append("HR reports: none\n");
            return;
        }
        sb.append("HR reports summary:\n");
        appendMapCounts(sb, "  Employees per team", reports.get("employeesByTeam"));
        appendMapCounts(sb, "  Leaves by status", reports.get("leavesByStatus"));
        appendMapCounts(sb, "  Tasks by status (HRM)", reports.get("tasksByStatus"));
    }

    private void appendNotifications(StringBuilder sb, JsonNode notifications) {
        if (unavailable(notifications)) {
            sb.append("Notifications: unavailable\n");
            return;
        }
        int count = notifications.isArray() ? notifications.size() : 0;
        sb.append("Notifications: ").append(count).append(" for current user\n");
    }

    private void appendSection(StringBuilder sb, String title, String body) {
        sb.append(title).append(": ").append(body).append("\n");
    }

    private void appendMapCounts(StringBuilder sb, String label, JsonNode mapNode) {
        if (mapNode == null || !mapNode.isObject() || mapNode.isEmpty()) {
            sb.append(label).append(": none\n");
            return;
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        mapNode.fields().forEachRemaining(e ->
                counts.put(e.getKey(), e.getValue().asLong(0)));
        sb.append(label).append(": ").append(formatCounts(counts)).append("\n");
    }

    private static String summarizeArray(JsonNode array, String... fields) {
        if (unavailable(array)) {
            return "unavailable";
        }
        if (!array.isArray()) {
            return "no data";
        }
        if (array.isEmpty()) {
            return "none (0)";
        }
        StringBuilder line = new StringBuilder(array.size() + " item(s)");
        Map<String, Long> byFirstField = fields.length > 1
                ? countByField(array, fields[1], false)
                : Map.of();
        if (!byFirstField.isEmpty()) {
            line.append(", by ").append(fields[1]).append(": ").append(formatCounts(byFirstField));
        }
        return line.toString();
    }

    private static String countLabel(JsonNode node, String label) {
        if (unavailable(node)) {
            return "unavailable";
        }
        if (node.isArray()) {
            return node.size() + " " + label;
        }
        if (node.isObject()) {
            return "1 " + label;
        }
        return "none";
    }

    private static boolean unavailable(JsonNode node) {
        return node != null && node.isTextual() && node.asText().startsWith("unavailable:");
    }

    private static String text(JsonNode node, String field, String fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return fallback;
        }
        return value.asText(fallback);
    }

    private static String roleName(JsonNode user) {
        JsonNode role = user.get("role");
        if (role == null) {
            return "UNKNOWN";
        }
        if (role.isTextual()) {
            return role.asText();
        }
        if (role.isObject()) {
            return role.path("name").asText("UNKNOWN");
        }
        return "UNKNOWN";
    }

    private static Map<String, Long> countByField(JsonNode array, String field, boolean roleNested) {
        Map<String, Long> counts = new LinkedHashMap<>();
        if (!array.isArray()) {
            return counts;
        }
        for (JsonNode item : array) {
            String key;
            if (roleNested && "role".equals(field)) {
                key = roleName(item);
            } else {
                key = text(item, field, "UNKNOWN");
            }
            counts.merge(key, 1L, Long::sum);
        }
        return counts;
    }

    private static long countWhere(ArrayNode array, String field, String expected) {
        return StreamSupport.stream(array.spliterator(), false)
                .filter(n -> expected.equals(text(n, field, "")))
                .count();
    }

    private static long countWhere(JsonNode array, String field, String expected) {
        if (!array.isArray()) {
            return 0;
        }
        return countWhere((ArrayNode) array, field, expected);
    }

    private static String formatCounts(Map<String, Long> counts) {
        StringBuilder sb = new StringBuilder();
        counts.forEach((k, v) -> {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(k).append("=").append(v);
        });
        return sb.isEmpty() ? "none" : sb.toString();
    }
}
