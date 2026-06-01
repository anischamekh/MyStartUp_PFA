package tn.iteam.chatbot.service;

import org.springframework.stereotype.Component;

@Component
public class ChatAnswerSanitizer {

    public String sanitize(String answer) {
        if (answer == null || answer.isBlank()) {
            return "I could not generate an answer. Please try again.";
        }
        String trimmed = answer.strip();
        if (looksLikeCodeOrJsonDump(trimmed)) {
            return "I can only summarize your HR and project data in plain language. "
                    + "Try a specific question such as \"How many active projects?\" or \"List users by role.\"";
        }
        return trimmed;
    }

    private static boolean looksLikeCodeOrJsonDump(String text) {
        if (text.contains("```") || text.contains("function ") || text.contains("const ")) {
            return true;
        }
        boolean hasJsonObject = text.contains("{") && text.contains("}");
        boolean hasSchemaHints = text.contains("\"users\"") || text.contains("// No ") || text.contains("[...]");
        return hasJsonObject && hasSchemaHints;
    }
}
