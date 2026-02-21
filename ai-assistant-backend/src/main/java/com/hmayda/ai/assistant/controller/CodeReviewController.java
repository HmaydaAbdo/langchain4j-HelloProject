package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.chatServices.StreamingChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/code-review")
public class CodeReviewController {

    private final StreamingChatService streamingChatService;

    public CodeReviewController(StreamingChatService streamingChatService) {
        this.streamingChatService = streamingChatService;
    }

    // POST /code-review?language=Java
    // Body: raw code as plain text
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter reviewCode(
            @RequestParam(defaultValue = "Java") String language,
            @RequestBody String code) {

        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout
        streamingChatService.reviewCode(language, code, emitter);
        return emitter;
    }
}
