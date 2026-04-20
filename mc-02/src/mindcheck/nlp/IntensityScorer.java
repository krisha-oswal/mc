package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 6: Detects intensity amplifiers and diminishers.
 * e.g., "very happy" → multiplier 1.4, "slightly sad" → multiplier 0.6
 */
public class IntensityScorer implements TextProcessor {

    private static final Map<String, Double> AMPLIFIERS = new LinkedHashMap<>();
    private static final Map<String, Double> DIMINISHERS = new LinkedHashMap<>();

    static {
        AMPLIFIERS.put("extremely",  1.7);
        AMPLIFIERS.put("incredibly", 1.6);
        AMPLIFIERS.put("absolutely", 1.5);
        AMPLIFIERS.put("really",     1.4);
        AMPLIFIERS.put("very",       1.4);
        AMPLIFIERS.put("so",         1.3);
        AMPLIFIERS.put("quite",      1.2);
        AMPLIFIERS.put("deeply",     1.5);
        AMPLIFIERS.put("terribly",   1.5);
        AMPLIFIERS.put("awfully",    1.4);
        AMPLIFIERS.put("completely", 1.5);
        AMPLIFIERS.put("totally",    1.4);
        AMPLIFIERS.put("utterly",    1.5);

        DIMINISHERS.put("slightly",   0.6);
        DIMINISHERS.put("somewhat",   0.7);
        DIMINISHERS.put("a bit",      0.7);
        DIMINISHERS.put("a little",   0.7);
        DIMINISHERS.put("kind of",    0.6);
        DIMINISHERS.put("sort of",    0.6);
        DIMINISHERS.put("rather",     0.8);
        DIMINISHERS.put("barely",     0.5);
        DIMINISHERS.put("hardly",     0.5);
        DIMINISHERS.put("almost",     0.7);
    }

    @Override
    public ProcessedText process(ProcessedText input) {
        String text = input.getCurrentText();
        double multiplier = 1.0;

        for (Map.Entry<String, Double> entry : AMPLIFIERS.entrySet()) {
            if (text.contains(entry.getKey())) {
                multiplier = Math.max(multiplier, entry.getValue());
            }
        }

        for (Map.Entry<String, Double> entry : DIMINISHERS.entrySet()) {
            if (text.contains(entry.getKey())) {
                multiplier = Math.min(multiplier, entry.getValue());
            }
        }

        // Also count exclamation marks for intensity
        long exclCount = text.chars().filter(c -> c == '!').count();
        if (exclCount >= 3) multiplier = Math.min(multiplier * 1.3, 2.0);

        input.setIntensityMultiplier(multiplier);
        return input;
    }

    @Override
    public String getStepName() { return "IntensityScorer"; }
}
