package com.hmayda.ai.assistant.services.aiServices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {

    @SystemMessage("""
            You are an expert chef and culinary advisor.
            Suggest creative, practical recipes with:
            - A short appetizing description
            - Ingredients list
            - Step-by-step instructions
            Be concise, enthusiastic, and always prioritize the dietary restriction.
            """)
    @UserMessage("Suggest a {{dietaryRestriction}} recipe using these ingredients: {{ingredients}}.")
    String suggestRecipe(@V("ingredients") String ingredients,
                         @V("dietaryRestriction") String dietaryRestriction);
}
