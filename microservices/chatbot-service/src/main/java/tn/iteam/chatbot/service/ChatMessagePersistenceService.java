package tn.iteam.chatbot.service;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.chatbot.entity.ChatMessage;
import tn.iteam.chatbot.repository.ChatMessageRepository;

@Service
public class ChatMessagePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ChatMessagePersistenceService.class);

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessagePersistenceService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long userId, String role, String content, boolean fromUser) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setRole(role == null ? "UNKNOWN" : role);
        message.setContent(content);
        message.setFromUser(fromUser);
        message.setCreatedAt(Instant.now());
        chatMessageRepository.saveAndFlush(message);
        log.debug("Saved chat message id={} userId={} fromUser={}", message.getId(), userId, fromUser);
    }
}
