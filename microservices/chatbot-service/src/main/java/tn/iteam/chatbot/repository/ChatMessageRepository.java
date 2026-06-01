package tn.iteam.chatbot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.chatbot.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);
}
