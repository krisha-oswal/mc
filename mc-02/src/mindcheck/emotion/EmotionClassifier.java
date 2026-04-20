package mindcheck.emotion;

import mindcheck.model.*;
import java.util.*;

/**
 * Main emotion classifier implementing the hybrid formula:
 *   finalScore = 0.6 * naiveBayesScore + 0.3 * ruleBasedScore + 0.1 * emojiScore
 *
 * Implements MoodAnalyser interface. Demonstrates: Polymorphism, Interface.
 */
public class EmotionClassifier implements MoodAnalyser {

    private static final double NB_WEIGHT   = 0.6;
    private static final double RULE_WEIGHT = 0.3;
    private static final double EMOJI_WEIGHT = 0.1;

    private final NaiveBayesClassifier nbClassifier;
    private final RuleBasedScorer ruleScorer;

    public EmotionClassifier() {
        this.nbClassifier = new NaiveBayesClassifier();
        this.ruleScorer   = new RuleBasedScorer();
    }

    @Override
    public EmotionResult analyse(ProcessedText pt) {
        // === 1. Naive Bayes scores (log-prob → normalize to [0,1]) ===
        List<String> allTokens = new ArrayList<>(pt.getTokens());
        allTokens.addAll(pt.getStems());
        Map<Emotion, Double> nbLogProbs = nbClassifier.classify(allTokens);
        Map<Emotion, Double> nbScores   = softmax(nbLogProbs);

        // === 2. Rule-based scores ===
        Map<Emotion, Double> ruleScores = ruleScorer.score(pt);

        // === 3. Emoji scores → distribute to closest emotion ===
        Map<Emotion, Double> emojiScores = computeEmojiScores(pt);

        // === 4. Hybrid combination ===
        Map<Emotion, Double> finalScores = new EnumMap<>(Emotion.class);
        for (Emotion e : Emotion.values()) {
            double nb   = nbScores.getOrDefault(e, 0.0);
            double rule = ruleScores.getOrDefault(e, 0.0);
            double emoji = emojiScores.getOrDefault(e, 0.0);
            double combined = NB_WEIGHT * nb + RULE_WEIGHT * rule + EMOJI_WEIGHT * emoji;
            finalScores.put(e, combined);
        }

        // === 5. Apply intensity multiplier ===
        double intensity = pt.getIntensityMultiplier();
        Emotion top = getTopEmotion(finalScores);
        // Amplify or dampen the top emotion score
        finalScores.put(top, Math.min(1.0, finalScores.get(top) * intensity));

        // === 6. Conflict detection: "I'm fine!!! 😭" ===
        top = getTopEmotion(finalScores); // re-evaluate
        top = resolveConflicts(pt, top, finalScores);

        // === 7. Build EmotionResult ===
        double topScore    = finalScores.get(top);
        double confidence  = computeConfidence(finalScores, top);

        double nbTopScore   = nbScores.getOrDefault(top, 0.0);
        double ruleTopScore = ruleScores.getOrDefault(top, 0.0);
        double emojiTop     = emojiScores.getOrDefault(top, 0.0);

        return new EmotionResult(top, confidence, nbTopScore, ruleTopScore, emojiTop, finalScores);
    }

    // ---- Helpers ----

    private Map<Emotion, Double> softmax(Map<Emotion, Double> logProbs) {
        double max = logProbs.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        Map<Emotion, Double> exp = new EnumMap<>(Emotion.class);
        double sum = 0;
        for (Map.Entry<Emotion, Double> e : logProbs.entrySet()) {
            double v = Math.exp(e.getValue() - max);
            exp.put(e.getKey(), v);
            sum += v;
        }
        Map<Emotion, Double> result = new EnumMap<>(Emotion.class);
        for (Map.Entry<Emotion, Double> e : exp.entrySet()) {
            result.put(e.getKey(), e.getValue() / sum);
        }
        return result;
    }

    private Map<Emotion, Double> computeEmojiScores(ProcessedText pt) {
        Map<Emotion, Double> scores = new EnumMap<>(Emotion.class);
        for (Emotion e : Emotion.values()) scores.put(e, 0.0);

        if (pt.getEmojiEmotionMap().isEmpty()) return scores;

        double avgEmojiScore = pt.getEmojiEmotionMap().values().stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Map avg score to closest emotion by valence
        Emotion closest = Emotion.NEUTRAL;
        double minDist = Double.MAX_VALUE;
        for (Emotion e : Emotion.values()) {
            double dist = Math.abs(e.getValence() - avgEmojiScore);
            if (dist < minDist) { minDist = dist; closest = e; }
        }
        scores.put(closest, Math.min(1.0, Math.abs(avgEmojiScore)));
        return scores;
    }

    private Emotion getTopEmotion(Map<Emotion, Double> scores) {
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Emotion.NEUTRAL);
    }

    /**
     * Conflict detection: e.g. "I'm fine!!! 😭"
     * If text has strong negation or contradictory emojis, adjust.
     */
    private Emotion resolveConflicts(ProcessedText pt, Emotion top, Map<Emotion, Double> scores) {
        // If positive top emotion but emojis are negative → flag and flip
        double emojiSentiment = pt.getEmojiEmotionMap().values().stream()
            .mapToDouble(Double::doubleValue).average().orElse(Double.NaN);

        if (!Double.isNaN(emojiSentiment)) {
            boolean posTop   = top.isPositive();
            boolean negEmoji = emojiSentiment < -0.3;
            boolean posEmoji = emojiSentiment > +0.3;

            if (posTop && negEmoji) {
                // Override: text says positive but emoji strongly negative
                // Find the top negative emotion
                return scores.entrySet().stream()
                    .filter(e -> e.getKey().isNegative())
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(top);
            }
            if (!posTop && posEmoji && top.isNegative()) {
                // Mixed: keep top but note conflict
                return top; // keep (negative wins in conflict)
            }
        }
        return top;
    }

    private double computeConfidence(Map<Emotion, Double> scores, Emotion top) {
        double topScore = scores.get(top);
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum == 0) return 0.5;
        // Ratio of top score to total — higher = more confident
        double ratio = topScore / sum;
        // Scale to [0.3, 0.99]
        return Math.min(0.99, 0.3 + ratio * 0.7 * Emotion.values().length);
    }
}
