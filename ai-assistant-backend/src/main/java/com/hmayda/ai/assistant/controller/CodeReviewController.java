package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.chatServices.StreamingChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/code-review")
public class CodeReviewController {

    private final StreamingChatService streamingChatService;

    public CodeReviewController(StreamingChatService streamingChatService) {
        this.streamingChatService = streamingChatService;
    }

    /**
     * POST /code-review?language=Java
     * Body: raw code as plain text
     *
     * Pass X-Conversation-Id to continue a review session (e.g. "fix issue #2 you mentioned").
     * Omit on first call to start a fresh review conversation.
     */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter reviewCode(
            @RequestParam(defaultValue = "Java") String language,
            @RequestBody String code,
            @RequestHeader(value = "X-Conversation-Id", required = false) String conversationId,
            HttpServletResponse response) {

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        response.setHeader("X-Conversation-Id", conversationId);

        SseEmitter emitter = new SseEmitter(120_000L);
        streamingChatService.reviewCode(language, code, emitter, conversationId);
        return emitter;
    }
}
