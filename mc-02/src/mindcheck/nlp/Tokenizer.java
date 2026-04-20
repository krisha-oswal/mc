package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 1: Tokenizes raw text into word tokens.
 * Lowercases text, splits on whitespace and punctuation (preserving emojis).
 */
public class Tokenizer implements TextProcessor {

    @Override
    public ProcessedText process(ProcessedText input) {
        String text = input.getCurrentText().toLowerCase().trim();

        // Split on spaces and common punctuation, keep apostrophes for contractions
        String[] raw = text.split("[\\s,;:!?\"()\\[\\]{}]+");

        List<String> tokens = new ArrayList<>();
        for (String token : raw) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }

        input.setTokens(tokens);
        input.setCurrentText(text);
        return input;
    }

    @Override
    public String getStepName() { return "Tokenizer"; }
}
