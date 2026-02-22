package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.aiServices.StreamingAssistant;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/story")
public class StoryController {

    private final StreamingAssistant streamingAssistant;

    public StoryController(StreamingAssistant streamingAssistant) {
        this.streamingAssistant = streamingAssistant;
    }

    /**
     * GET /story/stream?genre=thriller&character=Yassine&setting=Marrakech medina at night
     *
     * Pass X-Conversation-Id to continue a story session (e.g. "write a sequel").
     * Omit on first call to start a fresh story conversation.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStory(
            @RequestParam(defaultValue = "adventure") String genre,
            @RequestParam String character,
            @RequestParam(defaultValue = "a futuristic city") String setting,
            @RequestHeader(value = "X-Conversation-Id", required = false) String conversationId,
            HttpServletResponse response) {

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setHeader("X-Conversation-Id", conversationId);

        SseEmitter emitter = new SseEmitter(120_000L);
        final String finalConversationId = conversationId;

        streamingAssistant.generateStory(genre, character, setting, finalConversationId)
                .onNext(token -> {
                    try {
                        emitter.send(token);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onComplete(r -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }
}
