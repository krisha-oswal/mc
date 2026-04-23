package mindcheck.emotion;

import mindcheck.model.*;
import java.util.*;

/**
 * Hybrid emotion classifier.
 *
 * Formula:
 * finalScore = 0.55 * attentionScore
 * + 0.35 * ruleBasedScore
 * + 0.10 * emojiScore
 *
 * Replaces NaiveBayesClassifier with AttentionScorer.
 * AttentionScorer weights each word by its surrounding context
 * (amplifiers, negations, diminishers in a ±2 token window).
 *
 * Bug fix vs original:
 * The original softmax over log-probabilities spread scores evenly
 * across ALL 10 emotions, meaning neutral/negative emotions always
 * got a residual baseline score even for clearly positive text —
 * causing SAD/DEPRESSED to sometimes beat JOYFUL.
 *
 * Fix: AttentionScorer only scores emotions with explicit lexicon hits,
 * so unrelated emotions stay at 0. We also add a positivity guard:
 * if the raw text has clear positive signals and zero negative signals,
 * we suppress negative emotion scores before combining.
 */
public class EmotionClassifier implements MoodAnalyser {

    private static final double ATTN_WEIGHT = 0.55;
    private static final double RULE_WEIGHT = 0.35;
    private static final double EMOJI_WEIGHT = 0.10;

    private final AttentionScorer attentionScorer;
    private final RuleBasedScorer ruleScorer;

    public EmotionClassifier() {
        this.attentionScorer = new AttentionScorer();
        this.ruleScorer = new RuleBasedScorer();
    }

    @Override
    public EmotionResult analyse(ProcessedText pt) {

        // === 1. Attention-based scores ===
        List<String> allTokens = new ArrayList<>(pt.getTokens());
        allTokens.addAll(pt.getStems());
        Map<Emotion, Double> attnScores = attentionScorer.classify(allTokens);

        // === 2. Rule-based scores ===
        Map<Emotion, Double> ruleScores = ruleScorer.score(pt);

        // === 3. Emoji scores ===
        Map<Emotion, Double> emojiScores = computeEmojiScores(pt);

        // === 4. Polarity guard — prevents residual negative scores
        // from beating strong positive signals ===
        attnScores = applyPolarityGuard(pt, attnScores);

        // === 5. Hybrid combination ===
        Map<Emotion, Double> finalScores = new EnumMap<>(Emotion.class);
        for (Emotion e : Emotion.values()) {
            double attn = attnScores.getOrDefault(e, 0.0);
            double rule = ruleScores.getOrDefault(e, 0.0);
            double emoji = emojiScores.getOrDefault(e, 0.0);
            finalScores.put(e, ATTN_WEIGHT * attn + RULE_WEIGHT * rule + EMOJI_WEIGHT * emoji);
        }

        // === 6. Apply intensity multiplier to top emotion ===
        double intensity = pt.getIntensityMultiplier();
        Emotion top = getTopEmotion(finalScores);
        finalScores.put(top, Math.min(1.0, finalScores.get(top) * intensity));

        // === 7. Conflict resolution ("I'm fine!!! 😭") ===
        top = getTopEmotion(finalScores);
        top = resolveConflicts(pt, top, finalScores);

        // === 8. Build result ===
        double confidence = computeConfidence(finalScores, top);
        double attnTop = attnScores.getOrDefault(top, 0.0);
        double ruleTop = ruleScores.getOrDefault(top, 0.0);
        double emojiTop = emojiScores.getOrDefault(top, 0.0);

        return new EmotionResult(top, confidence, attnTop, ruleTop, emojiTop, finalScores);
    }

    // ── Polarity Guard ────────────────────────────────────────────────────────

    /**
     * If the entry has strong positive signals and NO negative signals at all,
     * suppress negative emotion scores to near-zero.
     *
     * This fixes the core bug: Naive Bayes log-prob softmax was giving residual
     * scores to SAD/DEPRESSED even for "I feel so happy today!" because every
     * emotion got a non-zero prior. AttentionScorer already fixes most of this,
     * but this guard is the safety net.
     */
    private Map<Emotion, Double> applyPolarityGuard(ProcessedText pt,
            Map<Emotion, Double> scores) {
        String text = pt.getOriginalText().toLowerCase();

        boolean hasNegativeSignal = text.contains("sad") || text.contains("depress")
                || text.contains("anxious") || text.contains("stress") || text.contains("angry")
                || text.contains("cry") || text.contains("upset") || text.contains("hopeless")
                || text.contains("lonely") || text.contains("panic") || text.contains("fear")
                || text.contains("overwhelm") || text.contains("exhausted") || text.contains("tired")
                || text.contains("hate") || text.contains("furious") || text.contains("rage")
                || pt.isHasNegation();

        boolean hasPositiveSignal = text.contains("happy") || text.contains("joy")
                || text.contains("amazing") || text.contains("wonderful") || text.contains("love")
                || text.contains("great") || text.contains("excited") || text.contains("awesome")
                || text.contains("fantastic") || text.contains("grateful") || text.contains("smile")
                || text.contains("glad") || text.contains("elated") || text.contains("cheerful");

        if (hasPositiveSignal && !hasNegativeSignal) {
            Map<Emotion, Double> guarded = new EnumMap<>(scores);
            for (Emotion e : Emotion.values()) {
                if (e.isNegative()) {
                    guarded.put(e, guarded.getOrDefault(e, 0.0) * 0.1);
                }
            }
            return guarded;
        }

        return scores;
    }

    // ── Emoji scores ──────────────────────────────────────────────────────────

    private Map<Emotion, Double> computeEmojiScores(ProcessedText pt) {
        Map<Emotion, Double> scores = new EnumMap<>(Emotion.class);
        for (Emotion e : Emotion.values())
            scores.put(e, 0.0);
        if (pt.getEmojiEmotionMap().isEmpty())
            return scores;

        double avgEmojiScore = pt.getEmojiEmotionMap().values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);

        Emotion closest = Emotion.NEUTRAL;
        double minDist = Double.MAX_VALUE;
        for (Emotion e : Emotion.values()) {
            double dist = Math.abs(e.getValence() - avgEmojiScore);
            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
        scores.put(closest, Math.min(1.0, Math.abs(avgEmojiScore)));
        return scores;
    }

    // ── Conflict resolution ───────────────────────────────────────────────────

    private Emotion resolveConflicts(ProcessedText pt, Emotion top,
            Map<Emotion, Double> scores) {
        double emojiSentiment = pt.getEmojiEmotionMap().values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(Double.NaN);

        if (!Double.isNaN(emojiSentiment)) {
            boolean posTop = top.isPositive();
            boolean negEmoji = emojiSentiment < -0.3;

            if (posTop && negEmoji) {
                return scores.entrySet().stream()
                        .filter(e -> e.getKey().isNegative())
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(top);
            }
        }
        return top;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Emotion getTopEmotion(Map<Emotion, Double> scores) {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Emotion.NEUTRAL);
    }

    private double computeConfidence(Map<Emotion, Double> scores, Emotion top) {
        double topScore = scores.get(top);
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum == 0)
            return 0.5;
        double ratio = topScore / sum;
        return Math.min(0.99, 0.3 + ratio * 0.7 * Emotion.values().length);
    }
}