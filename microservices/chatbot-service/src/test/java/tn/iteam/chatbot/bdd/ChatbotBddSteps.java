package tn.iteam.chatbot.bdd;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import tn.iteam.chatbot.service.ChatAnswerSanitizer;
import tn.iteam.chatbot.service.OllamaClient;

public class ChatbotBddSteps {

    private final OllamaClient ollamaClient = Mockito.mock(OllamaClient.class);
    private final ChatAnswerSanitizer sanitizer = new ChatAnswerSanitizer();
    private String response;

    @Given("a user is authenticated")
    public void aUserIsAuthenticated() {
        // Authentication is enforced by Spring Security in the real app; BDD focuses on chat flow.
    }

    @When("the user sends a question")
    public void theUserSendsAQuestion() {
        when(ollamaClient.ask(anyString(), anyString())).thenReturn("You have 2 active projects.");
        response = sanitizer.sanitize(ollamaClient.ask("system", "How many projects?"));
    }

    @Then("the chatbot returns a response")
    public void theChatbotReturnsAResponse() {
        assertFalse(response.isBlank());
    }
}
