package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatAnswerSanitizerTest {

    private final ChatAnswerSanitizer sanitizer = new ChatAnswerSanitizer();

    @Test
    void sanitize_blankReturnsFriendlyMessage() {
        assertEquals("I could not generate an answer. Please try again.", sanitizer.sanitize("  "));
    }

    @Test
    void sanitize_plainTextIsTrimmed() {
        assertEquals("Hello team", sanitizer.sanitize("  Hello team  "));
    }

    @Test
    void sanitize_jsonDumpIsRejected() {
        String json = "{\"users\": [{\"id\": 1}], // No sensitive data}";
        String result = sanitizer.sanitize(json);
        assertTrue(result.contains("plain language"));
    }
}
