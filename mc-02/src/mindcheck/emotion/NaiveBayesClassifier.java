package mindcheck.emotion;

import mindcheck.model.*;
import java.util.*;

/**
 * Custom Naive Bayes Classifier for emotion detection.
 * Trained on a built-in word→emotion probability lexicon.
 * P(emotion | words) ∝ P(emotion) * ∏ P(word | emotion)
 */
public class NaiveBayesClassifier {

    // word → Map<Emotion, probability>
    private static final Map<String, Map<Emotion, Double>> LEXICON = new HashMap<>();
    // Prior probability of each emotion
    private static final Map<Emotion, Double> PRIORS = new HashMap<>();

    static {
        // Set uniform priors
        for (Emotion e : Emotion.values()) PRIORS.put(e, 1.0 / Emotion.values().length);

        // Build lexicon: word → emotion scores
        addWord("happy",       JOYFUL(0.90), CONTENT(0.40));
        addWord("happiness",   JOYFUL(0.90), CONTENT(0.50));
        addWord("joy",         JOYFUL(0.85), EXCITED(0.40));
        addWord("joyful",      JOYFUL(0.90));
        addWord("wonderful",   JOYFUL(0.80), CONTENT(0.40));
        addWord("amazing",     EXCITED(0.85), JOYFUL(0.50));
        addWord("awesome",     EXCITED(0.80), JOYFUL(0.50));
        addWord("fantastic",   EXCITED(0.85), JOYFUL(0.50));
        addWord("great",       JOYFUL(0.75), CONTENT(0.50));
        addWord("good",        CONTENT(0.70), JOYFUL(0.40));
        addWord("excited",     EXCITED(0.90));
        addWord("thrilled",    EXCITED(0.85), JOYFUL(0.40));
        addWord("love",        JOYFUL(0.80), CONTENT(0.50));
        addWord("loved",       JOYFUL(0.80), CONTENT(0.50));
        addWord("peaceful",    CALM(0.85));
        addWord("calm",        CALM(0.90));
        addWord("relaxed",     CALM(0.85));
        addWord("serene",      CALM(0.80));
        addWord("content",     CONTENT(0.85));
        addWord("satisfied",   CONTENT(0.80), CALM(0.40));
        addWord("fine",        NEUTRAL(0.60), CONTENT(0.40));
        addWord("okay",        NEUTRAL(0.65));
        addWord("ok",          NEUTRAL(0.65));
        addWord("alright",     NEUTRAL(0.60));
        addWord("meh",         NEUTRAL(0.75));
        addWord("sad",         SAD(0.90));
        addWord("unhappy",     SAD(0.85));
        addWord("upset",       SAD(0.70), ANXIOUS(0.40));
        addWord("cry",         SAD(0.80));
        addWord("crying",      SAD(0.80));
        addWord("tears",       SAD(0.75));
        addWord("depressed",   DEPRESSED(0.90), SAD(0.50));
        addWord("hopeless",    DEPRESSED(0.85), SAD(0.50));
        addWord("worthless",   DEPRESSED(0.80), SAD(0.60));
        addWord("empty",       DEPRESSED(0.75), SAD(0.50));
        addWord("lonely",      SAD(0.70), DEPRESSED(0.60));
        addWord("miserable",   SAD(0.80), DEPRESSED(0.70));
        addWord("heartbroken", SAD(0.85), DEPRESSED(0.50));
        addWord("anxious",     ANXIOUS(0.90));
        addWord("anxiety",     ANXIOUS(0.90));
        addWord("worried",     ANXIOUS(0.85));
        addWord("nervous",     ANXIOUS(0.80));
        addWord("scared",      ANXIOUS(0.75));
        addWord("fear",        ANXIOUS(0.75));
        addWord("afraid",      ANXIOUS(0.80));
        addWord("panic",       ANXIOUS(0.85));
        addWord("panicking",   ANXIOUS(0.85));
        addWord("angry",       ANGRY(0.90));
        addWord("anger",       ANGRY(0.90));
        addWord("furious",     ANGRY(0.90));
        addWord("rage",        ANGRY(0.90));
        addWord("mad",         ANGRY(0.75));
        addWord("hate",        ANGRY(0.80));
        addWord("hating",      ANGRY(0.75));
        addWord("frustrated",  STRESSED(0.70), ANGRY(0.50));
        addWord("stress",      STRESSED(0.90));
        addWord("stressed",    STRESSED(0.90));
        addWord("overwhelmed", STRESSED(0.85));
        addWord("exhausted",   STRESSED(0.70), DEPRESSED(0.40));
        addWord("tired",       STRESSED(0.60), SAD(0.40));
        addWord("burden",      STRESSED(0.70), SAD(0.50));
        addWord("pressure",    STRESSED(0.80));
        addWord("dread",       ANXIOUS(0.80), DEPRESSED(0.50));

        // Stemmed variants
        addWord("happi",   JOYFUL(0.90), CONTENT(0.40));
        addWord("sad",     SAD(0.90));
        addWord("angri",   ANGRY(0.85));
        addWord("excit",   EXCITED(0.85));
        addWord("calm",    CALM(0.90));
        addWord("stress",  STRESSED(0.90));
        addWord("anxious", ANXIOUS(0.90));
        addWord("depress", DEPRESSED(0.90));
        addWord("relax",   CALM(0.85));
        addWord("content", CONTENT(0.80));

        // Negated forms handled by NEG_ prefix (scores are flipped later)
        addWord("NEG_happy",   SAD(0.80), ANXIOUS(0.30));
        addWord("NEG_good",    SAD(0.60), ANXIOUS(0.40));
        addWord("NEG_great",   SAD(0.60), NEUTRAL(0.40));
        addWord("NEG_fine",    ANXIOUS(0.50), SAD(0.50));
        addWord("NEG_sad",     JOYFUL(0.60), CONTENT(0.40));
        addWord("NEG_angry",   CALM(0.50), CONTENT(0.30));
        addWord("NEG_worried", CALM(0.60));
    }

    // Helpers to build emotion→prob maps cleanly
    private static Object[] JOYFUL(double p)   { return new Object[]{Emotion.JOYFUL,    p}; }
    private static Object[] EXCITED(double p)  { return new Object[]{Emotion.EXCITED,   p}; }
    private static Object[] CALM(double p)     { return new Object[]{Emotion.CALM,      p}; }
    private static Object[] CONTENT(double p)  { return new Object[]{Emotion.CONTENT,   p}; }
    private static Object[] NEUTRAL(double p)  { return new Object[]{Emotion.NEUTRAL,   p}; }
    private static Object[] ANXIOUS(double p)  { return new Object[]{Emotion.ANXIOUS,   p}; }
    private static Object[] SAD(double p)      { return new Object[]{Emotion.SAD,       p}; }
    private static Object[] ANGRY(double p)    { return new Object[]{Emotion.ANGRY,     p}; }
    private static Object[] STRESSED(double p) { return new Object[]{Emotion.STRESSED,  p}; }
    private static Object[] DEPRESSED(double p){ return new Object[]{Emotion.DEPRESSED, p}; }

    @SafeVarargs
    private static void addWord(String word, Object[]... emotionProbs) {
        Map<Emotion, Double> map = new HashMap<>();
        for (Object[] ep : emotionProbs) {
            map.put((Emotion) ep[0], (Double) ep[1]);
        }
        LEXICON.put(word, map);
    }

    /**
     * Returns a map of Emotion → log-probability score.
     */
    public Map<Emotion, Double> classify(List<String> tokens) {
        Map<Emotion, Double> logProbs = new HashMap<>();
        for (Emotion e : Emotion.values()) {
            logProbs.put(e, Math.log(PRIORS.get(e)));
        }

        for (String token : tokens) {
            Map<Emotion, Double> wordProbs = LEXICON.getOrDefault(token, null);
            if (wordProbs == null) continue;
            for (Emotion e : Emotion.values()) {
                double p = wordProbs.getOrDefault(e, 0.01); // Laplace smoothing
                logProbs.put(e, logProbs.get(e) + Math.log(p));
            }
        }

        return logProbs;
    }

    public boolean hasWordInLexicon(String word) {
        return LEXICON.containsKey(word);
    }
}
