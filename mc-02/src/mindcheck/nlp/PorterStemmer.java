package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 4: Custom Porter Stemmer (simplified) implementation.
 * Reduces words to their root form for better matching.
 * e.g., "running" → "run", "happiness" → "happi"
 */
public class PorterStemmer implements TextProcessor {

    @Override
    public ProcessedText process(ProcessedText input) {
        List<String> stemmed = new ArrayList<>();
        for (String token : input.getTokens()) {
            stemmed.add(stem(token));
        }
        input.setStems(stemmed);
        return input;
    }

    /**
     * Simplified Porter Stemmer — handles most common English suffixes.
     */
    public String stem(String word) {
        if (word == null || word.length() <= 2) return word;

        word = word.toLowerCase();

        // Step 1a — plural / third person
        if (word.endsWith("sses"))       word = word.substring(0, word.length() - 2);
        else if (word.endsWith("ies"))   word = word.substring(0, word.length() - 2);
        else if (word.endsWith("ss"))    { /* no change */ }
        else if (word.endsWith("s") && word.length() > 3) word = word.substring(0, word.length() - 1);

        // Step 1b — past tense / gerund
        if (word.endsWith("eed") && word.length() > 4) {
            word = word.substring(0, word.length() - 1);
        } else if (word.endsWith("ing")) {
            String base = word.substring(0, word.length() - 3);
            if (base.length() >= 2) word = base;
        } else if (word.endsWith("ed")) {
            String base = word.substring(0, word.length() - 2);
            if (base.length() >= 2) word = base;
        }

        // Step 2 — common suffixes
        if (word.endsWith("ational"))      word = word.substring(0, word.length() - 7) + "ate";
        else if (word.endsWith("tional"))  word = word.substring(0, word.length() - 2);
        else if (word.endsWith("enci"))    word = word.substring(0, word.length() - 1) + "e";
        else if (word.endsWith("anci"))    word = word.substring(0, word.length() - 1) + "e";
        else if (word.endsWith("izer"))    word = word.substring(0, word.length() - 1);
        else if (word.endsWith("iser"))    word = word.substring(0, word.length() - 1);
        else if (word.endsWith("ness"))    word = word.substring(0, word.length() - 4);
        else if (word.endsWith("ment"))    word = word.substring(0, word.length() - 4);
        else if (word.endsWith("ful"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("less"))    word = word.substring(0, word.length() - 4);
        else if (word.endsWith("ly"))      word = word.substring(0, word.length() - 2);
        else if (word.endsWith("ation"))   word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ism"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ist"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ity"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ous"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ive"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ize"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("ise"))     word = word.substring(0, word.length() - 3);
        else if (word.endsWith("er"))  { if (word.length() > 4) word = word.substring(0, word.length() - 2); }

        return word.length() >= 2 ? word : word; // safety
    }

    @Override
    public String getStepName() { return "PorterStemmer"; }
}
