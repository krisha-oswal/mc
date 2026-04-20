package mindcheck.nlp;

import mindcheck.model.*;
import java.util.*;

/**
 * Step 8 (final NLP step): Uses recent entry history to adjust context.
 * If user has been consistently negative, boosts negative emotion signals.
 */
public class ContextAnalyzer implements TextProcessor {
    private final List<EmotionResult> recentResults; // last 2-3 entries

    public ContextAnalyzer(List<EmotionResult> recentResults) {
        this.recentResults = recentResults != null ? recentResults : new ArrayList<>();
    }

    @Override
    public ProcessedText process(ProcessedText input) {
        if (recentResults.isEmpty()) return input;

        // Count how many recent entries were negative
        long negCount = recentResults.stream()
            .filter(r -> r.getPrimaryEmotion().isNegative())
            .count();

        // If 2+ recent entries are negative, amplify negative signal slightly
        if (negCount >= 2) {
            double current = input.getIntensityMultiplier();
            input.setIntensityMultiplier(current * 1.1); // slight boost
        }

        return input;
    }

    @Override
    public String getStepName() { return "ContextAnalyzer"; }
}
