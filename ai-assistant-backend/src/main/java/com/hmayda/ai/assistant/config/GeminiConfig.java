package com.hmayda.ai.assistant.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("gemini")
public class GeminiConfig {

    @Bean
    ChatLanguageModel chatLanguageModel(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model-name:gemini-2.5-flash}") String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequestsAndResponses(true)
                .temperature(0.7)
                .build();
    }

    @Bean
    StreamingChatLanguageModel streamingChatLanguageModel(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model-name:gemini-2.5-flash}") String modelName) {
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.7)
                .logRequestsAndResponses(true)
                .build();
    }
}