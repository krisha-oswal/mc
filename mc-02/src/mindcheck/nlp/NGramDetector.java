package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 7: Generates bigrams and trigrams from the token list.
 * Enables phrase-level detection (e.g., "feel good", "break down").
 */
public class NGramDetector implements TextProcessor {

    @Override
    public ProcessedText process(ProcessedText input) {
        List<String> tokens = input.getTokens();
        List<String> ngrams = new ArrayList<>();

        // Unigrams (already in tokens)
        ngrams.addAll(tokens);

        // Bigrams
        for (int i = 0; i < tokens.size() - 1; i++) {
            ngrams.add(tokens.get(i) + "_" + tokens.get(i + 1));
        }

        // Trigrams
        for (int i = 0; i < tokens.size() - 2; i++) {
            ngrams.add(tokens.get(i) + "_" + tokens.get(i + 1) + "_" + tokens.get(i + 2));
        }

        input.setNgrams(ngrams);
        return input;
    }

    @Override
    public String getStepName() { return "NGramDetector"; }
}
