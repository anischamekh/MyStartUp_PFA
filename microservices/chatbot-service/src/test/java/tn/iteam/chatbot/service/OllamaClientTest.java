package tn.iteam.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OllamaClientTest {

    private MockRestServiceServer server;
    private OllamaClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:11434");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OllamaClient(builder.build(), new ObjectMapper(), "llama3.2");
    }

    @AfterEach
    void tearDown() {
        server.reset();
    }

    @Test
    void ask_parsesOllamaResponse() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"message\":{\"content\":\"3 active projects\"}}",
                        MediaType.APPLICATION_JSON));

        String answer = client.ask("system", "How many projects?");
        assertEquals("3 active projects", answer);
        server.verify();
    }

    @Test
    void ask_returnsFallbackWhenBodyEmpty() {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        String answer = client.ask("system", "Hi");
        assertEquals("I could not generate an answer from the available data.", answer);
    }

    @Test
    void ask_returnsUnavailableMessageOnConnectionFailure() {
        OllamaClient offline = new OllamaClient("http://127.0.0.1:1", "llama3.2", new ObjectMapper());
        String answer = offline.ask("system", "Hi");
        assertTrue(answer.contains("unavailable"));
    }
}
