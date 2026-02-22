package com.hmayda.ai.assistant.services.aiServices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = {"nutritionEstimatorTool", "unitConverterTool", "ingredientSubstitutorTool"})
public interface Assistant {

    @SystemMessage("""
            You are an expert chef and culinary advisor.
            Suggest creative, practical recipes with:
            - A short appetizing description
            - Ingredients list
            - Step-by-step instructions
            Be concise, enthusiastic, and always prioritize the dietary restriction.
            You have access to tools — use them proactively:
            - Estimate nutrition when the user asks about calories or macros.
            - Convert units when the user mentions a measurement they need in a different unit.
            - Suggest ingredient substitutes when the user is missing something or has dietary restrictions.
            """)
    @UserMessage("Suggest a {{dietaryRestriction}} recipe using these ingredients: {{ingredients}}.")
    String suggestRecipe(@V("ingredients") String ingredients,
                         @V("dietaryRestriction") String dietaryRestriction,
                         @MemoryId String conversationId);

    @SystemMessage("""
            You are an expert chef and culinary advisor. You help users with anything food-related:
            suggesting recipes, answering nutrition questions, converting cooking measurements,
            finding ingredient substitutes, planning meals, and giving cooking tips.
            Be warm, enthusiastic, and practical in your responses.
            You have access to tools — use them proactively and automatically:
            - Call estimateNutrition when the user asks about calories, protein, carbs, or fat.
            - Call convertUnit when the user needs a measurement in a different unit.
            - Call findSubstitutes when the user is missing an ingredient or has a dietary restriction.
            Never refuse food or recipe topics — that is exactly your purpose.
            """)
    @UserMessage("{{message}}")
    String chat(@V("message") String message,
                @MemoryId String conversationId);
}
