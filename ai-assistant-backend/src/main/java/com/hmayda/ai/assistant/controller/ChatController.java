package com.hmayda.ai.assistant.controller;

import com.hmayda.ai.assistant.services.aiServices.Assistant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipe")
public class ChatController {

    private final Assistant assistant;

    public ChatController(Assistant assistant) {
        this.assistant = assistant;
    }

    // GET /recipe?ingredients=chicken,lemon,garlic&diet=gluten-free
    @GetMapping
    public String suggestRecipe(
            @RequestParam String ingredients,
            @RequestParam(defaultValue = "any") String diet) {
        return assistant.suggestRecipe(ingredients, diet);
    }
}
