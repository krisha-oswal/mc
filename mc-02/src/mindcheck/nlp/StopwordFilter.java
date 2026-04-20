package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 3: Removes common stopwords from the token list.
 * Preserves negation words (not, no, never) for the NegationHandler.
 */
public class StopwordFilter implements TextProcessor {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "is", "am", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "shall", "should",
        "may", "might", "must", "can", "could", "to", "of", "in", "on", "at", "by",
        "for", "with", "about", "against", "between", "into", "through", "during",
        "before", "after", "above", "below", "from", "up", "down", "out", "off",
        "over", "under", "again", "further", "then", "once", "and", "but", "or",
        "so", "yet", "both", "either", "neither", "whether", "although", "because",
        "since", "while", "i", "me", "my", "myself", "we", "our", "ours", "ourselves",
        "you", "your", "yours", "yourself", "he", "him", "his", "himself", "she",
        "her", "hers", "herself", "it", "its", "itself", "they", "them", "their",
        "theirs", "themselves", "what", "which", "who", "whom", "this", "that",
        "these", "those", "just", "also", "than", "too", "very", "s", "t", "just",
        "today", "day", "time", "week"
        // NOTE: "not", "no", "never", "nobody", "nothing" are intentionally EXCLUDED
        // so the NegationHandler can catch them
    ));

    @Override
    public ProcessedText process(ProcessedText input) {
        List<String> filtered = new ArrayList<>();
        for (String token : input.getTokens()) {
            if (!STOPWORDS.contains(token)) {
                filtered.add(token);
            }
        }
        input.setTokens(filtered);
        return input;
    }

    @Override
    public String getStepName() { return "StopwordFilter"; }
}
