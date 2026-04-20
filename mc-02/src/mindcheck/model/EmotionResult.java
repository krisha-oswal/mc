package mindcheck.model;

import java.util.Map;

/**
 * Holds the result of emotion analysis for a journal entry.
 */
public class EmotionResult {
    private final Emotion primaryEmotion;
    private final double confidence;         // 0.0 - 1.0
    private final double naiveBayesScore;
    private final double ruleBasedScore;
    private final double emojiScore;
    private final Map<Emotion, Double> emotionScores;  // all emotions scored

    public EmotionResult(Emotion primaryEmotion,
                         double confidence,
                         double naiveBayesScore,
                         double ruleBasedScore,
                         double emojiScore,
                         Map<Emotion, Double> emotionScores) {
        this.primaryEmotion   = primaryEmotion;
        this.confidence       = confidence;
        this.naiveBayesScore  = naiveBayesScore;
        this.ruleBasedScore   = ruleBasedScore;
        this.emojiScore       = emojiScore;
        this.emotionScores    = emotionScores;
    }

    public Emotion getPrimaryEmotion()         { return primaryEmotion; }
    public double getConfidence()              { return confidence; }
    public double getNaiveBayesScore()         { return naiveBayesScore; }
    public double getRuleBasedScore()          { return ruleBasedScore; }
    public double getEmojiScore()              { return emojiScore; }
    public Map<Emotion, Double> getEmotionScores() { return emotionScores; }

    public String getConfidenceLabel() {
        if (confidence >= 0.8) return "High";
        if (confidence >= 0.5) return "Medium";
        return "Low";
    }

    @Override
    public String toString() {
        return String.format("Emotion: %s | Confidence: %.0f%% (%s)",
            primaryEmotion, confidence * 100, getConfidenceLabel());
    }
}
