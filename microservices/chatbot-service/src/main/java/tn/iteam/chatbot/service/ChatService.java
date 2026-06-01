package tn.iteam.chatbot.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tn.iteam.chatbot.dto.ChatResponse;
import tn.iteam.chatbot.entity.ChatMessage;
import tn.iteam.chatbot.repository.ChatMessageRepository;
import tn.iteam.chatbot.security.SecurityContextHelper;
import tn.iteam.common.events.KafkaTopics;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ContextDataService contextDataService;
    private final ChatPromptBuilder promptBuilder;
    private final ChatAnswerSanitizer answerSanitizer;
    private final OllamaClient ollamaClient;
    private final ChatMessagePersistenceService messagePersistence;
    private final ChatMessageRepository chatMessageRepository;
    private final SecurityContextHelper securityContextHelper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ChatService(
            ContextDataService contextDataService,
            ChatPromptBuilder promptBuilder,
            ChatAnswerSanitizer answerSanitizer,
            OllamaClient ollamaClient,
            ChatMessagePersistenceService messagePersistence,
            ChatMessageRepository chatMessageRepository,
            SecurityContextHelper securityContextHelper,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.contextDataService = contextDataService;
        this.promptBuilder = promptBuilder;
        this.answerSanitizer = answerSanitizer;
        this.ollamaClient = ollamaClient;
        this.messagePersistence = messagePersistence;
        this.chatMessageRepository = chatMessageRepository;
        this.securityContextHelper = securityContextHelper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public ChatResponse ask(String bearerToken, String question, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required to save chat history");
        }

        String role = securityContextHelper.currentRole();
        messagePersistence.save(userId, role, question, true);

        String applicationData = contextDataService.buildContext(bearerToken);
        String answer = answerSanitizer.sanitize(
                ollamaClient.ask(promptBuilder.systemPrompt(), promptBuilder.userMessage(applicationData, question))
        );

        messagePersistence.save(userId, role, answer, false);

        publishChatLog(userId, role, question, answer);

        return new ChatResponse(answer, suggestionsForRole(role));
    }

    public List<ChatMessage> history(Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findTop30ByUserIdOrderByCreatedAtDesc(userId);
        Collections.reverse(messages);
        return messages;
    }

    public List<String> suggestions() {
        return suggestionsForRole(securityContextHelper.currentRole());
    }

    private void publishChatLog(Long userId, String role, String question, String answer) {
        try {
            kafkaTemplate.send(KafkaTopics.CHATBOT_LOGS, Map.of(
                    "userId", userId,
                    "role", role,
                    "question", question,
                    "answer", answer,
                    "occurredAt", Instant.now().toString()
            ));
        } catch (Exception ex) {
            log.warn("Could not publish chat log to Kafka (chat history was still saved): {}", ex.getMessage());
        }
    }

    private List<String> suggestionsForRole(String role) {
        if (role == null) {
            return List.of("What can you help me with?");
        }
        return switch (role) {
            case "EMPLOYEE" -> List.of(
                    "How many leave days do I have left?",
                    "What tasks are assigned to me?",
                    "Show my latest payroll information"
            );
            case "TEAM_LEADER" -> List.of(
                    "What is my team progress?",
                    "Which tasks are pending validation?",
                    "Show pending leave requests"
            );
            case "MANAGER" -> List.of(
                    "Give me project statistics",
                    "Which projects are delayed?",
                    "How is team performance?"
            );
            case "HR" -> List.of(
                    "Employee statistics overview",
                    "Leave statistics this month",
                    "Training management summary"
            );
            case "ADMIN" -> List.of(
                    "System overview",
                    "How many active projects?",
                    "User and team analytics"
            );
            default -> List.of("Ask about your HR or project data");
        };
    }
}
