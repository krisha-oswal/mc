package mindcheck.emotion;

import mindcheck.model.EmotionResult;
import mindcheck.model.ProcessedText;

/**
 * Interface for emotion classification.
 * Demonstrates: Interface / Polymorphism
 */
public interface MoodAnalyser {
    EmotionResult analyse(ProcessedText text);
}
