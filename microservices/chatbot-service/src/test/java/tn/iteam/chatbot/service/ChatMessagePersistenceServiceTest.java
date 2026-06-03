package tn.iteam.chatbot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.chatbot.repository.ChatMessageRepository;

@ExtendWith(MockitoExtension.class)
class ChatMessagePersistenceServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessagePersistenceService persistenceService;

    @Test
    void save_persistsMessage() {
        persistenceService.save(1L, "EMPLOYEE", "Hello", true);
        verify(chatMessageRepository).saveAndFlush(any());
    }
}
