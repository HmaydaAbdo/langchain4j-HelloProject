package com.hmayda.ai.assistant.memory;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production-grade memory that keeps the context window flat regardless of
 * conversation length.
 *
 * Structure injected into the LLM on every call:
 *
 *   [System prompt — persona & tools]          ~200 tokens  (fixed)
 *   [Summary — rolling compressed history]     ~100 tokens  (grows slowly, re-summarized)
 *   [Last maxRawMessages raw exchanges]         ~400 tokens  (sliding window)
 *   ─────────────────────────────────────────────────────────────────────
 *   Total context                              ~700 tokens  (regardless of turns)
 *
 * How it works:
 *   - New messages are appended normally until raw messages exceed maxRawMessages.
 *   - When threshold is hit, the oldest evictCount messages are extracted.
 *   - The LLM produces an updated summary: previous summary + evicted messages → new summary.
 *   - The evicted messages are replaced by the compact summary (~100 tokens).
 *   - The context window stays flat forever.
 *
 * Tool call messages (ToolExecutionRequestMessage, ToolExecutionResultMessage)
 * are included in the eviction budget and represented as [tool: name → result]
 * in the summary for compactness.
 */
public class SummarizingChatMemory implements ChatMemory {

    private static final String SUMMARY_TAG = "[SUMMARY]: ";

    private final Object id;

    /** Maximum number of non-system messages to keep raw in context. */
    private final int maxRawMessages;

    /** How many old messages to evict and summarize when threshold is hit. */
    private final int evictCount;

    /** Used only for summarization — lightweight call, not the main chat turn. */
    private final ChatLanguageModel summaryModel;

    private final ChatMemoryStore store;

    private SummarizingChatMemory(Builder builder) {
        this.id             = builder.id;
        this.maxRawMessages = builder.maxRawMessages;
        this.evictCount     = builder.evictCount;
        this.summaryModel   = builder.summaryModel;
        this.store          = builder.store;
    }

    // ─── ChatMemory API ──────────────────────────────────────────────────────

    @Override
    public Object id() {
        return id;
    }

    @Override
    public void add(ChatMessage message) {
        List<ChatMessage> messages = new ArrayList<>(store.getMessages(id));
        messages.add(message);

        long rawCount = messages.stream()
                .filter(m -> !(m instanceof SystemMessage))
                .count();

        if (rawCount > maxRawMessages) {
            summarizeAndEvict(messages);
        } else {
            store.updateMessages(id, messages);
        }
    }

    @Override
    public List<ChatMessage> messages() {
        return store.getMessages(id);
    }

    @Override
    public void clear() {
        store.deleteMessages(id);
    }

    // ─── Summarization logic ─────────────────────────────────────────────────

    private void summarizeAndEvict(List<ChatMessage> messages) {
        // Split into: persona (first system msg), existing summary (tagged system msg), raw
        SystemMessage persona          = null;
        SystemMessage existingSummary  = null;
        List<ChatMessage> rawMessages  = new ArrayList<>();

        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage sm) {
                if (sm.text().startsWith(SUMMARY_TAG)) {
                    existingSummary = sm;
                } else {
                    persona = sm;
                }
            } else {
                rawMessages.add(msg);
            }
        }

        // Determine how many to evict — never evict more than we have
        int toEvictCount = Math.min(evictCount, rawMessages.size() - 1);
        List<ChatMessage> toEvict = rawMessages.subList(0, toEvictCount);
        List<ChatMessage> toKeep  = rawMessages.subList(toEvictCount, rawMessages.size());

        // Build summarization prompt
        String updatedSummary = callSummaryModel(existingSummary, toEvict);

        // Reassemble: [persona] + [new summary] + [kept raw messages]
        List<ChatMessage> result = new ArrayList<>();
        if (persona != null)        result.add(persona);
        result.add(SystemMessage.from(SUMMARY_TAG + updatedSummary));
        result.addAll(toKeep);

        store.updateMessages(id, result);
    }

    private String callSummaryModel(SystemMessage existingSummary, List<ChatMessage> toEvict) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a conversation summarizer. Your task is to produce a concise, ")
              .append("factual summary of a conversation for a culinary AI assistant.\n\n");

        if (existingSummary != null) {
            prompt.append("Existing summary:\n")
                  .append(existingSummary.text().replace(SUMMARY_TAG, ""))
                  .append("\n\n");
        }

        prompt.append("New messages to incorporate:\n");
        for (ChatMessage msg : toEvict) {
            String line = formatForSummary(msg);
            if (!line.isBlank()) prompt.append(line).append("\n");
        }

        prompt.append("\nWrite a concise updated summary (2-4 sentences). ")
              .append("Capture: topics discussed, user dietary preferences, recipes suggested, ")
              .append("substitutions made, and any key context for future conversation turns. ")
              .append("Be factual and compact — this summary replaces the evicted messages.");

        return summaryModel.generate(prompt.toString());
    }

    /** Converts a chat message into a human-readable line for the summary prompt. */
    private String formatForSummary(ChatMessage message) {
        if (message instanceof UserMessage um) {
            return "User: " + um.singleText();
        } else if (message instanceof AiMessage am) {
            if (am.hasToolExecutionRequests()) {
                String tools = am.toolExecutionRequests().stream()
                        .map(r -> r.name() + "(" + r.arguments() + ")")
                        .collect(Collectors.joining(", "));
                return "Assistant: [called tools: " + tools + "]";
            }
            return "Assistant: " + am.text();
        } else if (message instanceof ToolExecutionResultMessage trm) {
            return "[Tool result: " + trm.toolName() + " → " + trm.text() + "]";
        }
        return "";
    }

    // ─── Builder ─────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object id;
        private int maxRawMessages = 10;
        private int evictCount     = 4;
        private ChatLanguageModel summaryModel;
        private ChatMemoryStore store;

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        /** Max number of non-system messages kept raw in context before summarization kicks in. */
        public Builder maxRawMessages(int maxRawMessages) {
            this.maxRawMessages = maxRawMessages;
            return this;
        }

        /** How many of the oldest messages to evict and fold into the summary each time. */
        public Builder evictCount(int evictCount) {
            this.evictCount = evictCount;
            return this;
        }

        public Builder summaryModel(ChatLanguageModel summaryModel) {
            this.summaryModel = summaryModel;
            return this;
        }

        public Builder store(ChatMemoryStore store) {
            this.store = store;
            return this;
        }

        public SummarizingChatMemory build() {
            if (id == null)           throw new IllegalStateException("id is required");
            if (summaryModel == null)  throw new IllegalStateException("summaryModel is required");
            if (store == null)         throw new IllegalStateException("store is required");
            if (evictCount >= maxRawMessages) {
                throw new IllegalStateException("evictCount must be less than maxRawMessages");
            }
            return new SummarizingChatMemory(this);
        }
    }
}
