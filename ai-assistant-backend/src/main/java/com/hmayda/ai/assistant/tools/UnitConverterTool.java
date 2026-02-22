package com.hmayda.ai.assistant.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts between common cooking measurements.
 * Covers volume (cups, tbsp, tsp, ml, fl oz, liters) and
 * weight (grams, oz, lbs, kg).
 * The LLM calls this automatically when the user asks to convert
 * measurements or uses a unit they're unfamiliar with.
 */
@Component
public class UnitConverterTool {

    // All volume units normalized to milliliters (ml)
    private static final Map<String, Double> VOLUME_TO_ML = new HashMap<>();

    // All weight units normalized to grams (g)
    private static final Map<String, Double> WEIGHT_TO_GRAMS = new HashMap<>();

    static {
        VOLUME_TO_ML.put("ml",          1.0);
        VOLUME_TO_ML.put("milliliter",  1.0);
        VOLUME_TO_ML.put("milliliters", 1.0);
        VOLUME_TO_ML.put("l",           1000.0);
        VOLUME_TO_ML.put("liter",       1000.0);
        VOLUME_TO_ML.put("liters",      1000.0);
        VOLUME_TO_ML.put("tsp",         4.929);
        VOLUME_TO_ML.put("teaspoon",    4.929);
        VOLUME_TO_ML.put("teaspoons",   4.929);
        VOLUME_TO_ML.put("tbsp",        14.787);
        VOLUME_TO_ML.put("tablespoon",  14.787);
        VOLUME_TO_ML.put("tablespoons", 14.787);
        VOLUME_TO_ML.put("cup",         236.588);
        VOLUME_TO_ML.put("cups",        236.588);
        VOLUME_TO_ML.put("fl oz",       29.574);
        VOLUME_TO_ML.put("fluid ounce", 29.574);
        VOLUME_TO_ML.put("pint",        473.176);
        VOLUME_TO_ML.put("pints",       473.176);
        VOLUME_TO_ML.put("quart",       946.353);
        VOLUME_TO_ML.put("quarts",      946.353);
        VOLUME_TO_ML.put("gallon",      3785.41);
        VOLUME_TO_ML.put("gallons",     3785.41);

        WEIGHT_TO_GRAMS.put("g",        1.0);
        WEIGHT_TO_GRAMS.put("gram",     1.0);
        WEIGHT_TO_GRAMS.put("grams",    1.0);
        WEIGHT_TO_GRAMS.put("kg",       1000.0);
        WEIGHT_TO_GRAMS.put("kilogram", 1000.0);
        WEIGHT_TO_GRAMS.put("kilograms",1000.0);
        WEIGHT_TO_GRAMS.put("oz",       28.3495);
        WEIGHT_TO_GRAMS.put("ounce",    28.3495);
        WEIGHT_TO_GRAMS.put("ounces",   28.3495);
        WEIGHT_TO_GRAMS.put("lb",       453.592);
        WEIGHT_TO_GRAMS.put("lbs",      453.592);
        WEIGHT_TO_GRAMS.put("pound",    453.592);
        WEIGHT_TO_GRAMS.put("pounds",   453.592);
    }

    @Tool("Convert a cooking measurement from one unit to another. " +
          "Supports volume units (cups, tbsp, tsp, ml, liters, fl oz, pint, quart, gallon) " +
          "and weight units (grams, kg, oz, lbs). " +
          "Use this when the user asks to convert measurements or needs a unit clarified.")
    public String convertUnit(double amount, String fromUnit, String toUnit) {
        String from = fromUnit.toLowerCase().trim();
        String to   = toUnit.toLowerCase().trim();

        // Try volume conversion
        if (VOLUME_TO_ML.containsKey(from) && VOLUME_TO_ML.containsKey(to)) {
            double ml     = amount * VOLUME_TO_ML.get(from);
            double result = ml / VOLUME_TO_ML.get(to);
            return String.format("%.4g %s = %.4g %s", amount, fromUnit, result, toUnit);
        }

        // Try weight conversion
        if (WEIGHT_TO_GRAMS.containsKey(from) && WEIGHT_TO_GRAMS.containsKey(to)) {
            double grams  = amount * WEIGHT_TO_GRAMS.get(from);
            double result = grams / WEIGHT_TO_GRAMS.get(to);
            return String.format("%.4g %s = %.4g %s", amount, fromUnit, result, toUnit);
        }

        // Mixed volume/weight conversion not possible without density
        if (VOLUME_TO_ML.containsKey(from) && WEIGHT_TO_GRAMS.containsKey(to) ||
            WEIGHT_TO_GRAMS.containsKey(from) && VOLUME_TO_ML.containsKey(to)) {
            return String.format(
                    "Cannot convert between volume (%s) and weight (%s) without knowing " +
                    "the ingredient's density. For water: 1 ml = 1 g.",
                    fromUnit, toUnit);
        }

        return String.format(
                "Unsupported unit(s): '%s' or '%s'. " +
                "Supported volume: cups, tbsp, tsp, ml, l, fl oz, pint, quart, gallon. " +
                "Supported weight: g, kg, oz, lb.",
                fromUnit, toUnit);
    }
}
