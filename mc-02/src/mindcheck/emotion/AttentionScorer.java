package mindcheck.emotion;

import mindcheck.model.*;
import java.util.*;

public class AttentionScorer {

    // ── Lexicon: word → Map<Emotion, base score> ──────────────────────────────
    private static final Map<String, Map<Emotion, Double>> LEXICON = new HashMap<>();

    // Context modifier word sets
    private static final Set<String> AMPLIFIERS = new HashSet<>(Arrays.asList(
            "very", "really", "extremely", "incredibly", "absolutely",
            "so", "deeply", "terribly", "completely", "totally", "utterly", "awfully"));
    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
            "not", "no", "never", "nobody", "nothing", "neither",
            "nor", "cant", "cannot", "dont", "doesnt", "didnt",
            "isnt", "wasnt", "arent", "werent", "havent", "hasnt",
            "wouldnt", "shouldnt", "couldnt", "n't"));
    private static final Set<String> DIMINISHERS = new HashSet<>(Arrays.asList(
            "slightly", "somewhat", "barely", "hardly", "almost",
            "a", "bit", "kind", "sort", "rather", "little"));

    static {
        // ── Positive emotions ──
        addWord("happy", em(Emotion.JOYFUL, 0.90), em(Emotion.CONTENT, 0.40));
        addWord("happiness", em(Emotion.JOYFUL, 0.90), em(Emotion.CONTENT, 0.50));
        addWord("joy", em(Emotion.JOYFUL, 0.85), em(Emotion.EXCITED, 0.40));
        addWord("joyful", em(Emotion.JOYFUL, 0.90));
        addWord("wonderful", em(Emotion.JOYFUL, 0.80), em(Emotion.CONTENT, 0.40));
        addWord("amazing", em(Emotion.EXCITED, 0.85), em(Emotion.JOYFUL, 0.60));
        addWord("awesome", em(Emotion.EXCITED, 0.80), em(Emotion.JOYFUL, 0.60));
        addWord("fantastic", em(Emotion.EXCITED, 0.85), em(Emotion.JOYFUL, 0.60));
        addWord("great", em(Emotion.JOYFUL, 0.80), em(Emotion.CONTENT, 0.50));
        addWord("good", em(Emotion.CONTENT, 0.75), em(Emotion.JOYFUL, 0.50));
        addWord("excited", em(Emotion.EXCITED, 0.90));
        addWord("thrilled", em(Emotion.EXCITED, 0.85), em(Emotion.JOYFUL, 0.50));
        addWord("love", em(Emotion.JOYFUL, 0.85), em(Emotion.CONTENT, 0.50));
        addWord("loved", em(Emotion.JOYFUL, 0.85), em(Emotion.CONTENT, 0.50));
        addWord("glad", em(Emotion.JOYFUL, 0.75), em(Emotion.CONTENT, 0.45));
        addWord("cheerful", em(Emotion.JOYFUL, 0.80));
        addWord("elated", em(Emotion.JOYFUL, 0.85), em(Emotion.EXCITED, 0.50));
        addWord("grateful", em(Emotion.JOYFUL, 0.75), em(Emotion.CONTENT, 0.60));
        addWord("thankful", em(Emotion.JOYFUL, 0.70), em(Emotion.CONTENT, 0.60));
        addWord("blessed", em(Emotion.JOYFUL, 0.75), em(Emotion.CONTENT, 0.55));
        addWord("smile", em(Emotion.JOYFUL, 0.70), em(Emotion.CONTENT, 0.50));
        addWord("laugh", em(Emotion.JOYFUL, 0.75), em(Emotion.EXCITED, 0.40));
        addWord("celebrate", em(Emotion.EXCITED, 0.80), em(Emotion.JOYFUL, 0.60));
        addWord("best", em(Emotion.JOYFUL, 0.70), em(Emotion.EXCITED, 0.50));

        // ── Calm / Content ──
        addWord("peaceful", em(Emotion.CALM, 0.88));
        addWord("calm", em(Emotion.CALM, 0.92));
        addWord("relaxed", em(Emotion.CALM, 0.85));
        addWord("serene", em(Emotion.CALM, 0.82));
        addWord("tranquil", em(Emotion.CALM, 0.80));
        addWord("content", em(Emotion.CONTENT, 0.88));
        addWord("satisfied", em(Emotion.CONTENT, 0.82), em(Emotion.CALM, 0.40));
        addWord("comfortable", em(Emotion.CONTENT, 0.75), em(Emotion.CALM, 0.45));
        addWord("okay", em(Emotion.NEUTRAL, 0.60), em(Emotion.CONTENT, 0.40));
        addWord("ok", em(Emotion.NEUTRAL, 0.60), em(Emotion.CONTENT, 0.40));
        addWord("fine", em(Emotion.NEUTRAL, 0.55), em(Emotion.CONTENT, 0.45));
        addWord("alright", em(Emotion.NEUTRAL, 0.60), em(Emotion.CONTENT, 0.40));
        addWord("meh", em(Emotion.NEUTRAL, 0.78));
        addWord("stable", em(Emotion.CALM, 0.65), em(Emotion.CONTENT, 0.55));

        // ── Negative emotions ── (given HIGHER base scores so they win clearly)
        addWord("sad", em(Emotion.SAD, 0.92));
        addWord("unhappy", em(Emotion.SAD, 0.88));
        addWord("upset", em(Emotion.SAD, 0.75), em(Emotion.ANXIOUS, 0.45));
        addWord("cry", em(Emotion.SAD, 0.85));
        addWord("crying", em(Emotion.SAD, 0.85));
        addWord("tears", em(Emotion.SAD, 0.78));
        addWord("lonely", em(Emotion.SAD, 0.78), em(Emotion.DEPRESSED, 0.55));
        addWord("miserable", em(Emotion.SAD, 0.85), em(Emotion.DEPRESSED, 0.65));
        addWord("heartbroken", em(Emotion.SAD, 0.88), em(Emotion.DEPRESSED, 0.50));
        addWord("depressed", em(Emotion.DEPRESSED, 0.95));
        addWord("hopeless", em(Emotion.DEPRESSED, 0.90), em(Emotion.SAD, 0.50));
        addWord("worthless", em(Emotion.DEPRESSED, 0.88), em(Emotion.SAD, 0.55));
        addWord("empty", em(Emotion.DEPRESSED, 0.82), em(Emotion.SAD, 0.50));
        addWord("numb", em(Emotion.DEPRESSED, 0.80));
        addWord("anxious", em(Emotion.ANXIOUS, 0.92));
        addWord("anxiety", em(Emotion.ANXIOUS, 0.92));
        addWord("worried", em(Emotion.ANXIOUS, 0.88));
        addWord("nervous", em(Emotion.ANXIOUS, 0.82));
        addWord("scared", em(Emotion.ANXIOUS, 0.80));
        addWord("fear", em(Emotion.ANXIOUS, 0.80));
        addWord("panic", em(Emotion.ANXIOUS, 0.88));
        addWord("angry", em(Emotion.ANGRY, 0.92));
        addWord("furious", em(Emotion.ANGRY, 0.92));
        addWord("rage", em(Emotion.ANGRY, 0.92));
        addWord("mad", em(Emotion.ANGRY, 0.78));
        addWord("hate", em(Emotion.ANGRY, 0.85));
        addWord("frustrated", em(Emotion.STRESSED, 0.72), em(Emotion.ANGRY, 0.55));
        addWord("stressed", em(Emotion.STRESSED, 0.92));
        addWord("stress", em(Emotion.STRESSED, 0.92));
        addWord("overwhelmed", em(Emotion.STRESSED, 0.88));
        addWord("exhausted", em(Emotion.STRESSED, 0.75), em(Emotion.DEPRESSED, 0.45));
        addWord("tired", em(Emotion.STRESSED, 0.65), em(Emotion.SAD, 0.40));
        addWord("pressure", em(Emotion.STRESSED, 0.82));
        addWord("dread", em(Emotion.ANXIOUS, 0.82), em(Emotion.DEPRESSED, 0.50));

        // ── Stemmed variants ──
        addWord("happi", em(Emotion.JOYFUL, 0.90), em(Emotion.CONTENT, 0.40));
        addWord("angri", em(Emotion.ANGRY, 0.88));
        addWord("excit", em(Emotion.EXCITED, 0.88));
        addWord("stress", em(Emotion.STRESSED, 0.92));
        addWord("depress", em(Emotion.DEPRESSED, 0.92));
        addWord("relax", em(Emotion.CALM, 0.85));
        addWord("worri", em(Emotion.ANXIOUS, 0.85));
        addWord("scarei", em(Emotion.ANXIOUS, 0.80));
        addWord("lone", em(Emotion.SAD, 0.75));
    }

    // ── Helper builder ────────────────────────────────────────────────────────
    private static Map.Entry<Emotion, Double> em(Emotion e, double score) {
        return new AbstractMap.SimpleEntry<>(e, score);
    }

    @SafeVarargs
    private static void addWord(String word, Map.Entry<Emotion, Double>... entries) {
        Map<Emotion, Double> map = new HashMap<>();
        for (Map.Entry<Emotion, Double> entry : entries)
            map.put(entry.getKey(), entry.getValue());
        LEXICON.put(word, map);
    }

    // ── Main classify method ──────────────────────────────────────────────────

    /**
     * Attention-based classification.
     * Returns Map<Emotion, score> in [0, 1] — same contract as old NaiveBayes.
     */
    public Map<Emotion, Double> classify(List<String> tokens) {
        Map<Emotion, Double> scores = new EnumMap<>(Emotion.class);
        for (Emotion e : Emotion.values())
            scores.put(e, 0.0);

        if (tokens.isEmpty())
            return scores;

        // For each token, compute attention-weighted score
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            Map<Emotion, Double> wordScores = LEXICON.get(token);
            if (wordScores == null)
                continue;

            // Compute context attention by scanning ±2 window
            double attention = computeContextAttention(tokens, i);

            // Apply attention weight to each emotion score for this word
            for (Map.Entry<Emotion, Double> entry : wordScores.entrySet()) {
                Emotion emotion = entry.getKey();
                double weighted = entry.getValue() * attention;
                scores.put(emotion, scores.get(emotion) + weighted);
            }
        }

        // Normalize so values are in [0, 1]
        return normalize(scores);
    }

    /**
     * Computes the attention weight for token at position i
     * by scanning its ±2 context window.
     *
     * This is the core "attention" idea: a word's emotional weight
     * is modulated by what surrounds it.
     */
    private double computeContextAttention(List<String> tokens, int i) {
        double attention = 1.0;
        int window = 2;

        for (int j = Math.max(0, i - window); j <= Math.min(tokens.size() - 1, i + window); j++) {
            if (j == i)
                continue;
            String neighbour = tokens.get(j);

            if (NEGATIONS.contains(neighbour)) {
                attention *= -0.8; // flip and dampen — "not happy" → negative signal
            } else if (AMPLIFIERS.contains(neighbour)) {
                attention *= 1.5; // "very happy" → stronger signal
            } else if (DIMINISHERS.contains(neighbour)) {
                attention *= 0.55; // "slightly sad" → weaker signal
            }
        }

        return attention;
    }

    /**
     * Normalize scores to [0, 1] range using min-max scaling.
     * Negative attention values (from negation) are handled by
     * clamping to 0 — they instead boost the opposite polarity
     * via the context itself reducing positive word contributions.
     */
    private Map<Emotion, Double> normalize(Map<Emotion, Double> scores) {
        double max = scores.values().stream().mapToDouble(Math::abs).max().orElse(1.0);
        if (max == 0)
            return scores;

        Map<Emotion, Double> normalized = new EnumMap<>(Emotion.class);
        for (Map.Entry<Emotion, Double> e : scores.entrySet()) {
            // Clamp negatives to 0 — negative attention reduces the word's contribution
            // rather than boosting the opposite emotion (that's handled by NegationHandler)
            normalized.put(e.getKey(), Math.max(0.0, e.getValue() / max));
        }
        return normalized;
    }

    public boolean hasWordInLexicon(String word) {
        return LEXICON.containsKey(word);
    }
}