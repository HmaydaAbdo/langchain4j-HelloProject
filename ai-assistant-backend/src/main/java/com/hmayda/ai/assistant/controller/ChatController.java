package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.aiServices.Assistant;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/recipe")
public class ChatController {

    private final Assistant assistant;

    public ChatController(Assistant assistant) {
        this.assistant = assistant;
    }

    /**
     * GET /recipe?ingredients=chicken,lemon,garlic&diet=gluten-free
     *
     * Pass X-Conversation-Id header to continue an existing conversation.
     * On the first call, omit the header — a new ID is generated and returned
     * in the response header so the client can reuse it on follow-up calls.
     */
    @GetMapping
    public String suggestRecipe(
            @RequestParam String ingredients,
            @RequestParam(defaultValue = "any") String diet,
            @RequestHeader(value = "X-Conversation-Id", required = false) String conversationId,
            HttpServletResponse response) {

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setHeader("X-Conversation-Id", conversationId);

        return assistant.suggestRecipe(ingredients, diet, conversationId);
    }
}
