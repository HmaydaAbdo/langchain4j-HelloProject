package com.hmayda.ai.assistant.services.aiServices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface StreamingAssistant {

    @SystemMessage("""
            You are a talented and imaginative story writer.
            Write vivid, engaging short stories with rich descriptions,
            dynamic characters, and an unexpected twist at the end.
            """)
    @UserMessage("""
            Write a short {{genre}} story (around 200 words) featuring a character named {{character}},
            set in {{setting}}.
            """)
    TokenStream generateStory(@V("genre") String genre,
                              @V("character") String character,
                              @V("setting") String setting);
}
