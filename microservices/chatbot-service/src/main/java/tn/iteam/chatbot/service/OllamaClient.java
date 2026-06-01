package tn.iteam.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OllamaClient(
            @Value("${app.ollama.base-url}") String baseUrl,
            @Value("${app.ollama.model}") String model,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.model = model;
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = RestClient.builder().baseUrl(normalized).build();
    }

    public String ask(String systemPrompt, String userMessage) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("stream", false);

        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content", userMessage);

        ObjectNode options = body.putObject("options");
        options.put("temperature", 0.1);
        options.put("num_predict", 400);

        try {
            JsonNode response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                return "No response from Ollama.";
            }
            String content = response.path("message").path("content").asText("");
            if (!content.isBlank()) {
                return content;
            }
            return "I could not generate an answer from the available data.";
        } catch (RestClientException ex) {
            log.warn("Ollama request failed: {}", ex.getMessage());
            return "The AI assistant is unavailable. Start Ollama on your machine, ensure the model \""
                    + model + "\" is installed (ollama pull " + model + "), and set OLLAMA_BASE_URL if needed.";
        }
    }
}
