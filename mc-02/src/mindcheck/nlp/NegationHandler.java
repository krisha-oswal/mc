package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 5: Detects negation patterns like "not happy", "never felt good".
 * Marks negated phrases so the classifier can flip their sentiment.
 */
public class NegationHandler implements TextProcessor {

    private static final Set<String> NEGATION_WORDS = new HashSet<>(Arrays.asList(
        "not", "no", "never", "nobody", "nothing", "neither", "nowhere",
        "nor", "n't", "cant", "cannot", "won't", "don't", "doesn't",
        "didn't", "isn't", "wasn't", "aren't", "weren't", "haven't",
        "hasn't", "hadn't", "wouldn't", "shouldn't", "couldn't"
    ));

    @Override
    public ProcessedText process(ProcessedText input) {
        List<String> tokens = input.getTokens();
        List<String> processed = new ArrayList<>();
        boolean negating = false;
        int negationWindow = 0; // how many words left to negate

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (NEGATION_WORDS.contains(token)) {
                negating = true;
                negationWindow = 3; // negate next 3 words
                input.setHasNegation(true);
                processed.add(token);
            } else if (negating && negationWindow > 0) {
                // Mark negated token with NEG_ prefix
                String negated = "NEG_" + token;
                processed.add(negated);
                input.addNegatedPhrase(token);
                negationWindow--;
                if (negationWindow == 0) negating = false;
            } else {
                processed.add(token);
                negating = false;
            }
        }

        input.setTokens(processed);
        return input;
    }

    @Override
    public String getStepName() { return "NegationHandler"; }
}
