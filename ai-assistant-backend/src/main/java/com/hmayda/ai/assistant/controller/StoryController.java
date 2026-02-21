package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.aiServices.StreamingAssistant;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/story")
public class StoryController {

    private final StreamingAssistant streamingAssistant;

    public StoryController(StreamingAssistant streamingAssistant) {
        this.streamingAssistant = streamingAssistant;
    }

    // GET /story/stream?genre=thriller&character=Yassine&setting=Marrakech medina at night
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStory(
            @RequestParam(defaultValue = "adventure") String genre,
            @RequestParam String character,
            @RequestParam(defaultValue = "a futuristic city") String setting) {

        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        streamingAssistant.generateStory(genre, character, setting)
                .onNext(token -> {
                    try {
                        emitter.send(token);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onComplete(response -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }
}
