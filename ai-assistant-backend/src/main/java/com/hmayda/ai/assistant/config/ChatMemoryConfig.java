package com.hmayda.ai.assistant.config;

import com.hmayda.ai.assistant.memory.SummarizingChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Production Memory Configuration — summarizing, conversationId-scoped.
 *
 * Strategy: instead of injecting raw conversation history (which grows
 * indefinitely), the context window is kept flat at all times:
 *
 *   [System prompt]      ~200 tokens  fixed
 *   [Rolling summary]    ~100 tokens  compressed history, updated incrementally
 *   [Last 10 raw msgs]   ~400 tokens  sliding window for conversational coherence
 *   ─────────────────────────────────────────────────────────────────────────
 *   Total                ~700 tokens  regardless of conversation length
 *
 * Summarization triggers when raw messages exceed 10.
 * The oldest 4 messages are evicted and folded into the rolling summary.
 * One extra LLM call is made at that point — subsequent turns are free again.
 *
 * To upgrade storage: swap InMemoryChatMemoryStore for a Redis-backed
 * implementation of ChatMemoryStore — no other changes needed.
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * Shared in-memory store — single source of truth for all conversations.
     * Replace with RedisBackedChatMemoryStore for horizontal scaling.
     */
    @Bean
    public InMemoryChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * Summarizing memory provider — one isolated SummarizingChatMemory per conversationId.
     *
     * maxRawMessages(10): keep up to 10 raw messages before summarizing.
     * evictCount(4):      when triggered, fold the oldest 4 into the summary.
     * Result after eviction: [summary] + [6 raw messages] — always under control.
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(InMemoryChatMemoryStore store,
                                                 ChatLanguageModel chatLanguageModel) {
        return conversationId -> SummarizingChatMemory.builder()
                .id(conversationId)
                .maxRawMessages(10)
                .evictCount(4)
                .summaryModel(chatLanguageModel)
                .store(store)
                .build();
    }
}
