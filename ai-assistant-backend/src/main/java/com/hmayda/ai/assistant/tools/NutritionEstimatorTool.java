package com.hmayda.ai.assistant.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Estimates nutritional values for common cooking ingredients.
 * Values are per 100g and sourced from standard nutritional references.
 * The LLM calls this automatically when the user asks about calories,
 * macros, or nutritional content of a recipe or ingredient.
 */
@Component
public class NutritionEstimatorTool {

    // Nutrition per 100g: { calories, protein(g), carbs(g), fat(g) }
    private static final Map<String, double[]> NUTRITION_DB = new HashMap<>();

    static {
        // Proteins
        NUTRITION_DB.put("chicken breast",  new double[]{165, 31, 0,   3.6});
        NUTRITION_DB.put("chicken",         new double[]{165, 31, 0,   3.6});
        NUTRITION_DB.put("beef",            new double[]{250, 26, 0,   17});
        NUTRITION_DB.put("salmon",          new double[]{208, 20, 0,   13});
        NUTRITION_DB.put("tuna",            new double[]{130, 29, 0,   1});
        NUTRITION_DB.put("egg",             new double[]{155, 13, 1.1, 11});
        NUTRITION_DB.put("eggs",            new double[]{155, 13, 1.1, 11});
        NUTRITION_DB.put("shrimp",          new double[]{99,  24, 0.2, 0.3});
        NUTRITION_DB.put("tofu",            new double[]{76,  8,  1.9, 4.8});
        NUTRITION_DB.put("lentils",         new double[]{116, 9,  20,  0.4});
        NUTRITION_DB.put("chickpeas",       new double[]{164, 9,  27,  2.6});

        // Carbs & grains
        NUTRITION_DB.put("rice",            new double[]{130, 2.7, 28, 0.3});
        NUTRITION_DB.put("pasta",           new double[]{158, 5.8, 31, 0.9});
        NUTRITION_DB.put("bread",           new double[]{265, 9,  49,  3.2});
        NUTRITION_DB.put("potato",          new double[]{77,  2,  17,  0.1});
        NUTRITION_DB.put("sweet potato",    new double[]{86,  1.6, 20, 0.1});
        NUTRITION_DB.put("oats",            new double[]{389, 17, 66,  7});
        NUTRITION_DB.put("flour",           new double[]{364, 10, 76,  1});

        // Vegetables
        NUTRITION_DB.put("broccoli",        new double[]{34,  2.8, 7,  0.4});
        NUTRITION_DB.put("spinach",         new double[]{23,  2.9, 3.6, 0.4});
        NUTRITION_DB.put("tomato",          new double[]{18,  0.9, 3.9, 0.2});
        NUTRITION_DB.put("tomatoes",        new double[]{18,  0.9, 3.9, 0.2});
        NUTRITION_DB.put("onion",           new double[]{40,  1.1, 9,  0.1});
        NUTRITION_DB.put("garlic",          new double[]{149, 6.4, 33, 0.5});
        NUTRITION_DB.put("carrot",          new double[]{41,  0.9, 10, 0.2});
        NUTRITION_DB.put("mushroom",        new double[]{22,  3.1, 3.3, 0.3});
        NUTRITION_DB.put("mushrooms",       new double[]{22,  3.1, 3.3, 0.3});
        NUTRITION_DB.put("zucchini",        new double[]{17,  1.2, 3.1, 0.3});
        NUTRITION_DB.put("bell pepper",     new double[]{31,  1,   6,  0.3});

        // Dairy & fats
        NUTRITION_DB.put("butter",          new double[]{717, 0.9, 0.1, 81});
        NUTRITION_DB.put("olive oil",       new double[]{884, 0,   0,  100});
        NUTRITION_DB.put("milk",            new double[]{42,  3.4, 5,  1});
        NUTRITION_DB.put("cheese",          new double[]{402, 25, 1.3, 33});
        NUTRITION_DB.put("yogurt",          new double[]{59,  3.5, 5,  3.3});
        NUTRITION_DB.put("cream",           new double[]{340, 2.1, 2.8, 36});

        // Other
        NUTRITION_DB.put("sugar",           new double[]{387, 0,  100, 0});
        NUTRITION_DB.put("honey",           new double[]{304, 0.3, 82, 0});
        NUTRITION_DB.put("lemon",           new double[]{29,  1.1, 9,  0.3});
        NUTRITION_DB.put("avocado",         new double[]{160, 2,   9,  15});
    }

    @Tool("Estimate the nutritional content (calories, protein, carbs, fat) of an ingredient " +
          "given its name and weight in grams. Use this when the user asks about nutrition, " +
          "calories, or macros for any ingredient or recipe.")
    public String estimateNutrition(String ingredient, double grams) {
        String key = ingredient.toLowerCase().trim();
        double[] values = NUTRITION_DB.get(key);

        if (values == null) {
            // Try partial match
            values = NUTRITION_DB.entrySet().stream()
                    .filter(e -> key.contains(e.getKey()) || e.getKey().contains(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (values == null) {
            return String.format(
                    "Nutrition data not available for '%s'. " +
                    "Please note this tool covers common ingredients only.", ingredient);
        }

        double factor = grams / 100.0;
        return String.format(
                "Nutrition for %.0fg of %s: %.0f kcal | Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg",
                grams, ingredient,
                values[0] * factor,
                values[1] * factor,
                values[2] * factor,
                values[3] * factor
        );
    }
}
