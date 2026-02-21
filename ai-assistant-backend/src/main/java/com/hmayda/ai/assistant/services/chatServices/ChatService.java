package com.hmayda.ai.assistant.services.chatServices;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatLanguageModel chatModel;

    public ChatService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public String getTravelItinerary(String destination, String days, String travelStyle) {
        String prompt = String.format("""
                You are an expert travel advisor.
                Create a %s-day travel itinerary for %s for a traveler with a "%s" travel style.
                For each day include:
                - Morning, afternoon and evening activities
                - A local restaurant recommendation
                - One practical travel tip
                Keep it realistic and inspiring.
                """, days, destination, travelStyle);

        return chatModel.generate(prompt);
    }
}
