package com.hmayda.ai.assistant.services.chatServices;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class StreamingChatService {

    private final StreamingChatLanguageModel streamingModel;
    private final ChatMemoryProvider memoryProvider;

    public StreamingChatService(StreamingChatLanguageModel streamingModel, ChatMemoryProvider memoryProvider) {
        this.streamingModel = streamingModel;
        this.memoryProvider = memoryProvider;
    }

    public void reviewCode(String language, String code, SseEmitter emitter, String conversationId) {
        ChatMemory memory = memoryProvider.get(conversationId);

        // Inject system role only at the start of a new conversation
        if (memory.messages().isEmpty()) {
            memory.add(SystemMessage.from("""
                    You are a senior software engineer doing thorough code reviews.
                    When reviewing code provide: a quality score (1-10), identified bugs or
                    potential issues, performance improvements, best practices and readability
                    suggestions, and a short final verdict.
                    On follow-up questions, refer to the code already shared — the user does
                    not need to paste it again.
                    """));
        }

        String userPrompt = String.format("""
                Review the following %s code:
                ```%s
                %s
                ```
                """, language, language.toLowerCase(), code);

        memory.add(UserMessage.from(userPrompt));

        streamingModel.generate(memory.messages(), new StreamingResponseHandler<>() {

            // Accumulate tokens so the full AI response can be saved to memory on completion
            private final StringBuilder fullResponse = new StringBuilder();

            @Override
            public void onNext(String token) {
                fullResponse.append(token);
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                memory.add(response.content());
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });
    }
}
