package tn.iteam.chatbot.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<String> suggestedQuestions
) {}
