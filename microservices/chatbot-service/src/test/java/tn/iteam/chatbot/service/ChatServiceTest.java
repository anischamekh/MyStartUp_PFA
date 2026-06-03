package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tn.iteam.chatbot.dto.ChatResponse;
import tn.iteam.chatbot.entity.ChatMessage;
import tn.iteam.chatbot.repository.ChatMessageRepository;
import tn.iteam.chatbot.security.SecurityContextHelper;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ContextDataService contextDataService;
    @Mock
    private ChatPromptBuilder promptBuilder;
    @Mock
    private ChatAnswerSanitizer answerSanitizer;
    @Mock
    private OllamaClient ollamaClient;
    @Mock
    private ChatMessagePersistenceService messagePersistence;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private SecurityContextHelper securityContextHelper;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    void ask_requiresUserId() {
        assertThrows(IllegalArgumentException.class, () -> chatService.ask("token", "hi", null));
    }

    @Test
    void ask_returnsSanitizedAnswer() {
        when(securityContextHelper.currentRole()).thenReturn("EMPLOYEE");
        when(contextDataService.buildContext("token")).thenReturn("ctx");
        when(promptBuilder.systemPrompt()).thenReturn("system");
        when(promptBuilder.userMessage("ctx", "projects?")).thenReturn("user-msg");
        when(ollamaClient.ask("system", "user-msg")).thenReturn("raw");
        when(answerSanitizer.sanitize("raw")).thenReturn("2 projects");

        ChatResponse response = chatService.ask("token", "projects?", 7L);

        assertEquals("2 projects", response.answer());
        verify(messagePersistence).save(7L, "EMPLOYEE", "projects?", true);
        verify(messagePersistence).save(7L, "EMPLOYEE", "2 projects", false);
    }

    @Test
    void history_returnsMessagesInChronologicalOrder() {
        ChatMessage newer = new ChatMessage();
        ChatMessage older = new ChatMessage();
        when(chatMessageRepository.findTop30ByUserIdOrderByCreatedAtDesc(3L))
                .thenReturn(new ArrayList<>(List.of(newer, older)));

        List<ChatMessage> history = chatService.history(3L);

        assertEquals(2, history.size());
        assertEquals(older, history.get(0));
        assertEquals(newer, history.get(1));
    }

    @Test
    void suggestions_delegatesToRole() {
        when(securityContextHelper.currentRole()).thenReturn("ADMIN");
        List<String> suggestions = chatService.suggestions();
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("System overview")));
    }

    @Test
    void suggestions_forManagerRole() {
        when(securityContextHelper.currentRole()).thenReturn("MANAGER");
        assertTrue(chatService.suggestions().stream().anyMatch(s -> s.contains("project")));
    }

    @Test
    void suggestions_forHrRole() {
        when(securityContextHelper.currentRole()).thenReturn("HR");
        assertTrue(chatService.suggestions().stream().anyMatch(s -> s.contains("Employee")));
    }
}
