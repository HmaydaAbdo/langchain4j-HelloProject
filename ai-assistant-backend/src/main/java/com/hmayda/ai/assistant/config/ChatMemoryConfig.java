package com.hmayda.ai.assistant.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Layer 1 Memory Configuration — conversationId-scoped, no auth required.
 *
 * Strategy: client generates a UUID on first call and passes it via the
 * "X-Conversation-Id" request header. The backend scopes a ChatMemory to
 * that ID, so conversation history is preserved across turns for the same ID.
 *
 * Storage: InMemoryChatMemoryStore (lives in JVM heap).
 * — Good for development and single-instance deployments.
 * — To scale horizontally or survive restarts, swap with a Redis-backed
 *   implementation of ChatMemoryStore (Layer 1 upgrade path).
 *
 * Window: last 20 messages per conversation.
 * — Keeps token usage predictable and avoids context-window overflow.
 * — Tune maxMessages based on your LLM's context limit and cost tolerance.
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * Shared in-memory store — single source of truth for all conversations.
     * Replace this bean with a RedisBackedChatMemoryStore for production scale.
     */
    @Bean
    public InMemoryChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * Provider called by @AiService interfaces annotated with @MemoryId.
     * Also injected manually into ChatService and StreamingChatService.
     *
     * Each unique conversationId gets its own isolated MessageWindowChatMemory.
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(InMemoryChatMemoryStore store) {
        return conversationId -> MessageWindowChatMemory.builder()
                .id(conversationId)
                .maxMessages(20)
                .chatMemoryStore(store)
                .build();
    }
}
