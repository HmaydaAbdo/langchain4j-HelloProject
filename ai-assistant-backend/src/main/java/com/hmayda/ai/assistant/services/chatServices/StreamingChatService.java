package com.hmayda.ai.assistant.services.chatServices;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class StreamingChatService {

    private final StreamingChatLanguageModel streamingModel;

    public StreamingChatService(StreamingChatLanguageModel streamingModel) {
        this.streamingModel = streamingModel;
    }

    public void reviewCode(String language, String code, SseEmitter emitter) {
        String prompt = String.format("""
                You are a senior software engineer doing a thorough code review.
                Analyze the following %s code and provide:
                1. A quality score (1-10)
                2. Identified bugs or potential issues
                3. Performance improvements
                4. Best practices and readability suggestions
                5. A short final verdict

                Code to review:
                ```%s
                %s
                ```
                """, language, language.toLowerCase(), code);

        streamingModel.generate(prompt, new StreamingResponseHandler<>() {
            @Override
            public void onNext(String token) {
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });
    }
}
