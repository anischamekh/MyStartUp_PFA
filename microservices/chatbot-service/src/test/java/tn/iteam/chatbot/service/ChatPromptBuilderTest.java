package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatPromptBuilderTest {

    private final ChatPromptBuilder builder = new ChatPromptBuilder();

    @Test
    void systemPrompt_containsRules() {
        assertTrue(builder.systemPrompt().contains("STRICT RULES"));
    }

    @Test
    void userMessage_embedsDataAndQuestion() {
        String msg = builder.userMessage("projects: 2", "  How many?  ");
        assertTrue(msg.contains("APPLICATION DATA:"));
        assertTrue(msg.contains("projects: 2"));
        assertTrue(msg.contains("How many?"));
    }
}
