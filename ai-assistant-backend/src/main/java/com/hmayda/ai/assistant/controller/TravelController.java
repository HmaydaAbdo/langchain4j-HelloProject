package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.chatServices.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/travel")
public class TravelController {

    private final ChatService chatService;

    public TravelController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * GET /travel?destination=Marrakech&days=3&style=cultural
     *
     * Pass X-Conversation-Id header to continue an existing conversation
     * (e.g. "make day 2 more adventurous"). Omit on first call to start fresh.
     */
    @GetMapping
    public String getItinerary(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") String days,
            @RequestParam(defaultValue = "cultural") String style,
            @RequestHeader(value = "X-Conversation-Id", required = false) String conversationId,
            HttpServletResponse response) {

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setHeader("X-Conversation-Id", conversationId);

        return chatService.getTravelItinerary(destination, days, style, conversationId);
    }
}
