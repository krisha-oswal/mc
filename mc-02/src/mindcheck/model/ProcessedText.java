package mindcheck.model;

import java.util.*;

/**
 * Carries NLP state through the Chain of Responsibility pipeline.
 */
public class ProcessedText {
    private String originalText;
    private String currentText;
    private List<String> tokens;
    private List<String> stems;
    private List<String> ngrams;
    private Map<String, Double> emojiEmotionMap;   // emoji → emotion score
    private double intensityMultiplier;
    private boolean hasNegation;
    private List<String> negatedPhrases;

    public ProcessedText(String originalText) {
        this.originalText = originalText;
        this.currentText  = originalText;
        this.tokens       = new ArrayList<>();
        this.stems        = new ArrayList<>();
        this.ngrams       = new ArrayList<>();
        this.emojiEmotionMap     = new LinkedHashMap<>();
        this.intensityMultiplier = 1.0;
        this.hasNegation  = false;
        this.negatedPhrases = new ArrayList<>();
    }

    // --- Getters ---
    public String getOriginalText()            { return originalText; }
    public String getCurrentText()             { return currentText; }
    public List<String> getTokens()            { return tokens; }
    public List<String> getStems()             { return stems; }
    public List<String> getNgrams()            { return ngrams; }
    public Map<String, Double> getEmojiEmotionMap() { return emojiEmotionMap; }
    public double getIntensityMultiplier()     { return intensityMultiplier; }
    public boolean isHasNegation()             { return hasNegation; }
    public List<String> getNegatedPhrases()    { return negatedPhrases; }

    // --- Setters / Mutators ---
    public void setCurrentText(String text)    { this.currentText = text; }
    public void setTokens(List<String> t)      { this.tokens = t; }
    public void setStems(List<String> s)       { this.stems = s; }
    public void setNgrams(List<String> n)      { this.ngrams = n; }
    public void addEmojiEmotion(String emoji, double score) {
        emojiEmotionMap.put(emoji, score);
    }
    public void setIntensityMultiplier(double m) { this.intensityMultiplier = m; }
    public void setHasNegation(boolean b)      { this.hasNegation = b; }
    public void addNegatedPhrase(String phrase){ this.negatedPhrases.add(phrase); }

    @Override
    public String toString() {
        return String.format("ProcessedText{tokens=%d, stems=%d, ngrams=%d, emojis=%d, intensity=%.2f, negation=%b}",
            tokens.size(), stems.size(), ngrams.size(), emojiEmotionMap.size(),
            intensityMultiplier, hasNegation);
    }
}
