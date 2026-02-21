package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.chatServices.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/travel")
public class TravelController {

    private final ChatService chatService;

    public TravelController(ChatService chatService) {
        this.chatService = chatService;
    }

    // GET /travel?destination=Marrakech&days=3&style=cultural
    @GetMapping
    public String getItinerary(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") String days,
            @RequestParam(defaultValue = "cultural") String style) {
        return chatService.getTravelItinerary(destination, days, style);
    }
}
