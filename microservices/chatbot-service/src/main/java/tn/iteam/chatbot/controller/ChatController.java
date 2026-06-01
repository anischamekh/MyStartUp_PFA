package tn.iteam.chatbot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tn.iteam.common.openapi.OpenApiExamples;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.chatbot.dto.ChatRequest;
import tn.iteam.chatbot.dto.ChatResponse;
import tn.iteam.chatbot.entity.ChatMessage;
import tn.iteam.chatbot.service.ChatService;
import tn.iteam.common.security.SharedJwtService;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "AI assistant powered by Ollama")
public class ChatController {

    private final ChatService chatService;
    private final SharedJwtService jwtService;

    public ChatController(
            ChatService chatService,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.chatService = chatService;
        this.jwtService = new SharedJwtService(secret, expirationMs, expirationMs);
    }

    @PostMapping("/ask")
    @Operation(summary = "Ask the AI assistant", description = "Uses real application data based on user role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI answer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(examples = @ExampleObject(value = OpenApiExamples.ERROR_RESPONSE)))
    })
    public ChatResponse ask(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);
        return chatService.ask(token, request.question(), userId);
    }

    @GetMapping("/history")
    @Operation(summary = "Conversation history", description = "Returns latest messages for current user")
    public List<ChatMessage> history(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);
        return chatService.history(userId);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Suggested questions", description = "Role-aware suggested prompts")
    public List<String> suggestions() {
        return chatService.suggestions();
    }
}
