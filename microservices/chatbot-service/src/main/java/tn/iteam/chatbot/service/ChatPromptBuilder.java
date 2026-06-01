package tn.iteam.chatbot.service;

import org.springframework.stereotype.Component;

@Component
public class ChatPromptBuilder {

    private static final String SYSTEM_RULES = """
            You are MyStartUp Assistant, an internal HR and project management helper.

            STRICT RULES:
            - Answer ONLY using facts from APPLICATION DATA. Never invent numbers, users, or projects.
            - Write plain English for business users. Be concise (1-5 sentences for simple questions).
            - NEVER output JSON, code, JavaScript, pseudocode, or placeholder syntax like [...] or // comments.
            - NEVER explain how to fix code or query databases.
            - If APPLICATION DATA shows 0 items, say clearly there are zero (do not say "need more data" if counts are given).
            - For greetings (hello, hi), reply briefly and offer help with HR/project data.
            - Use bullet lists only when the user asks for a list or there are more than 5 items to name.
            """;

    public String systemPrompt() {
        return SYSTEM_RULES;
    }

    public String userMessage(String applicationData, String question) {
        return """
                APPLICATION DATA:
                %s

                USER QUESTION:
                %s
                """.formatted(applicationData, question.trim());
    }
}
