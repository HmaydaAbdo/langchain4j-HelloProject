package com.hmayda.ai.assistant.services.chatServices;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatLanguageModel chatModel;
    private final ChatMemoryProvider memoryProvider;

    public ChatService(ChatLanguageModel chatModel, ChatMemoryProvider memoryProvider) {
        this.chatModel = chatModel;
        this.memoryProvider = memoryProvider;
    }

    public String getTravelItinerary(String destination, String days, String travelStyle, String conversationId) {
        ChatMemory memory = memoryProvider.get(conversationId);

        // Inject system role only at the start of a new conversation
        if (memory.messages().isEmpty()) {
            memory.add(SystemMessage.from("""
                    You are an expert travel advisor.
                    For each itinerary include morning, afternoon and evening activities,
                    a local restaurant recommendation, and one practical travel tip per day.
                    Keep it realistic and inspiring.
                    """));
        }

        String userPrompt = String.format(
                "Create a %s-day travel itinerary for %s for a traveler with a \"%s\" travel style.",
                days, destination, travelStyle);

        memory.add(UserMessage.from(userPrompt));

        Response<AiMessage> response = chatModel.generate(memory.messages());
        memory.add(response.content());

        return response.content().text();
    }
}
