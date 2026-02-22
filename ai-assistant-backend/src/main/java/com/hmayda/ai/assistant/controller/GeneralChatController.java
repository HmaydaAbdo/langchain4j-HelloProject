package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.aiServices.Assistant;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class GeneralChatController {

    private final Assistant assistant;

    public GeneralChatController(Assistant assistant) {
        this.assistant = assistant;
    }

    /**
     * POST /chat
     * Body: plain text message (e.g. "how many calories in 150g of salmon?")
     *
     * Free-form endpoint — no ingredient or diet params required.
     * Shares the same memory and tools as /recipe, so conversations
     * can freely mix recipe requests, nutrition questions, unit conversions,
     * and substitute lookups within the same X-Conversation-Id session.
     */
    @PostMapping(consumes = "text/plain", produces = "text/plain")
    public String chat(
            @RequestBody String message,
            @RequestHeader(value = "X-Conversation-Id", required = false) String conversationId,
            HttpServletResponse response) {

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setHeader("X-Conversation-Id", conversationId);

        return assistant.chat(message, conversationId);
    }
}
