package com.hmayda.ai.assistant.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suggests common cooking substitutes for ingredients.
 * Covers dietary restrictions (vegan, gluten-free), allergens (dairy, eggs, nuts),
 * and pantry emergencies ("I don't have X").
 * The LLM calls this automatically when the user says they don't have an ingredient
 * or asks for a vegan/allergy-friendly alternative.
 */
@Component
public class IngredientSubstitutorTool {

    // ingredient → list of { substitute, reason }
    private static final Map<String, List<String[]>> SUBSTITUTES = new HashMap<>();

    static {
        // Eggs
        SUBSTITUTES.put("egg", List.of(
                new String[]{"1 tbsp ground flaxseed + 3 tbsp water (flax egg)", "binding — works in muffins, pancakes, cookies"},
                new String[]{"¼ cup mashed banana",                               "binding + moisture — adds slight sweetness"},
                new String[]{"¼ cup unsweetened applesauce",                      "moisture + binding — neutral flavor"},
                new String[]{"3 tbsp aquafaba (chickpea brine)",                  "emulsification + lift — best for meringues and mayo"}
        ));

        // Butter
        SUBSTITUTES.put("butter", List.of(
                new String[]{"equal amount of coconut oil",                        "vegan, works in baking and sautéing"},
                new String[]{"¾ amount of olive oil",                              "vegan, better for savory cooking"},
                new String[]{"equal amount of Greek yogurt",                       "for baking — adds moisture, reduces fat"},
                new String[]{"equal amount of mashed avocado",                     "vegan, for baking — adds healthy fats"}
        ));

        // Milk
        SUBSTITUTES.put("milk", List.of(
                new String[]{"equal amount of oat milk",                           "dairy-free, neutral flavor, great for baking"},
                new String[]{"equal amount of almond milk",                        "dairy-free, slightly nutty flavor"},
                new String[]{"equal amount of soy milk",                           "dairy-free, closest protein content to cow milk"},
                new String[]{"equal amount of coconut milk (canned)",              "richer, dairy-free, adds coconut flavor"}
        ));

        // Flour (all-purpose)
        SUBSTITUTES.put("flour", List.of(
                new String[]{"equal amount of almond flour",                       "gluten-free, adds moisture and nuttiness"},
                new String[]{"equal amount of oat flour",                          "gluten-free (if certified), mild flavor"},
                new String[]{"equal amount of rice flour",                         "gluten-free, light and neutral flavor"},
                new String[]{"¾ amount of coconut flour + extra liquid",           "gluten-free, highly absorbent — adjust moisture"}
        ));

        // Sugar (white)
        SUBSTITUTES.put("sugar", List.of(
                new String[]{"equal amount of coconut sugar",                      "lower GI, caramel-like flavor"},
                new String[]{"¾ amount of honey (reduce other liquids by ¼ cup)", "natural sweetener, adds moisture"},
                new String[]{"¾ amount of maple syrup (reduce liquids by 3 tbsp)","vegan, distinct flavor"},
                new String[]{"equal amount of date sugar",                         "whole food sweetener, rich flavor"}
        ));

        // Cream
        SUBSTITUTES.put("cream", List.of(
                new String[]{"equal amount of full-fat coconut cream",             "dairy-free, rich texture, slight coconut flavor"},
                new String[]{"equal amount of cashew cream",                       "dairy-free, very neutral flavor"},
                new String[]{"equal amount of evaporated milk",                    "lower fat, similar consistency"}
        ));

        // Yogurt
        SUBSTITUTES.put("yogurt", List.of(
                new String[]{"equal amount of coconut yogurt",                     "dairy-free, similar texture and tang"},
                new String[]{"equal amount of sour cream",                         "similar tang and creaminess"},
                new String[]{"equal amount of buttermilk",                         "for baking — adds tang and moisture"}
        ));

        // Breadcrumbs
        SUBSTITUTES.put("breadcrumbs", List.of(
                new String[]{"equal amount of crushed oats",                       "gluten-free option, adds texture"},
                new String[]{"equal amount of crushed crackers",                   "quick pantry swap"},
                new String[]{"equal amount of ground nuts (almonds, walnuts)",     "gluten-free, adds nuttiness"},
                new String[]{"equal amount of panko",                              "lighter, crispier result"}
        ));

        // Lemon juice
        SUBSTITUTES.put("lemon juice", List.of(
                new String[]{"equal amount of lime juice",                         "same acidity, slightly different flavor"},
                new String[]{"half the amount of white wine vinegar",              "same acid effect, more pungent"},
                new String[]{"half the amount of apple cider vinegar",             "mild acidity, slight apple flavor"}
        ));

        // Olive oil
        SUBSTITUTES.put("olive oil", List.of(
                new String[]{"equal amount of avocado oil",                        "neutral flavor, high smoke point — great for cooking"},
                new String[]{"equal amount of sunflower oil",                      "neutral, budget-friendly"},
                new String[]{"equal amount of coconut oil",                        "adds slight coconut flavor, solid at room temp"}
        ));

        // Garlic
        SUBSTITUTES.put("garlic", List.of(
                new String[]{"⅛ tsp garlic powder per clove",                     "convenient pantry swap, less pungent"},
                new String[]{"1 tsp garlic-infused oil per clove",                 "great for low-FODMAP diets"},
                new String[]{"equal amount of shallots",                           "milder, slightly sweet garlic alternative"}
        ));

        // Honey
        SUBSTITUTES.put("honey", List.of(
                new String[]{"equal amount of maple syrup",                        "vegan, thinner consistency, distinct flavor"},
                new String[]{"equal amount of agave nectar",                       "vegan, very neutral flavor, lower GI"},
                new String[]{"equal amount of date syrup",                         "vegan, rich flavor with caramel notes"}
        ));

        // Soy sauce
        SUBSTITUTES.put("soy sauce", List.of(
                new String[]{"equal amount of tamari",                             "gluten-free, nearly identical flavor"},
                new String[]{"equal amount of coconut aminos",                     "soy-free, slightly sweeter and less salty"},
                new String[]{"equal amount of Worcestershire sauce",               "deeper umami, not vegan"}
        ));
    }

    @Tool("Suggest cooking substitutes for an ingredient. " +
          "Use this when the user says they don't have an ingredient, has a dietary restriction " +
          "(vegan, gluten-free, dairy-free), or has a food allergy. " +
          "Returns a list of substitutes with the reason each one works.")
    public String findSubstitutes(String ingredient) {
        String key = ingredient.toLowerCase().trim();

        List<String[]> options = SUBSTITUTES.get(key);

        if (options == null) {
            // Try partial match
            options = SUBSTITUTES.entrySet().stream()
                    .filter(e -> key.contains(e.getKey()) || e.getKey().contains(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (options == null) {
            return String.format(
                    "No substitutes found for '%s' in the database. " +
                    "You can ask me to suggest one based on the recipe context.", ingredient);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Substitutes for %s:\n", ingredient));
        for (int i = 0; i < options.size(); i++) {
            sb.append(String.format("  %d. %s — %s\n", i + 1, options.get(i)[0], options.get(i)[1]));
        }
        return sb.toString().trim();
    }
}
