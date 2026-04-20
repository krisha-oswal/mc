package mindcheck.suggestion;

import mindcheck.model.EmotionResult;
import java.util.List;

/**
 * Strategy interface for generating wellness suggestions.
 * Demonstrates: Strategy Pattern
 */
public interface MoodSuggestionStrategy {
    List<String> getSuggestions(EmotionResult result);
    String getStrategyName();
}
