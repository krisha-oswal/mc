package mindcheck.suggestion;

import mindcheck.model.EmotionResult;
import java.util.*;

/**
 * Orchestrates all suggestion strategies and combines their output.
 * Applies confidence scoring to prioritize suggestions.
 */
public class SuggestionEngine {

    private final List<MoodSuggestionStrategy> strategies;

    public SuggestionEngine() {
        strategies = new ArrayList<>();
        strategies.add(new InstantTipEngine());
        strategies.add(new PatternSuggestionEngine());
        strategies.add(new AdaptivePromptEngine());
    }

    /**
     * Returns all suggestions from all strategies.
     */
    public Map<String, List<String>> getAllSuggestions(EmotionResult result) {
        Map<String, List<String>> all = new LinkedHashMap<>();
        for (MoodSuggestionStrategy strategy : strategies) {
            List<String> suggestions = strategy.getSuggestions(result);
            if (!suggestions.isEmpty()) {
                all.put(strategy.getStrategyName(), suggestions);
            }
        }
        return all;
    }

    /**
     * Returns a flat merged list (top suggestions from each strategy).
     */
    public List<String> getTopSuggestions(EmotionResult result, int max) {
        List<String> combined = new ArrayList<>();
        for (MoodSuggestionStrategy strategy : strategies) {
            List<String> s = strategy.getSuggestions(result);
            if (!s.isEmpty()) combined.add(s.get(0)); // top from each
        }
        return combined.subList(0, Math.min(max, combined.size()));
    }
}
