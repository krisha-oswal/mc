package mindcheck.analytics;

import mindcheck.model.*;
import java.util.*;

/**
 * Observer: Detects emotional anomalies and conflicts.
 *
 * Added detections vs original:
 * 1. All-caps shouting — "I AM SO DONE" signals high emotional intensity.
 * 2. Smart 😭 disambiguation — 😭 is NOT always sadness. People use it for
 * "this is hilarious", "I'm dead 😭", "so cute 😭". We check surrounding
 * context to decide if it's genuine distress or casual hyperbole.
 * 3. Original checks retained: conflict, excessive punctuation, contradiction.
 */
public class AnomalyDetector implements AnalyticsObserver {

    private final List<String> detectedAnomalies = new ArrayList<>();

    // Words that indicate 😭 is being used casually / humorously (not real crying)
    private static final Set<String> CASUAL_SOB_CONTEXT = new HashSet<>(Arrays.asList(
            "funny", "hilarious", "lol", "lmao", "dead", "dying", "cute", "adorable",
            "omg", "literally", "cannot", "can't", "pls", "please", "bro", "dude",
            "help", "why", "same", "mood", "screaming", "crying", "this", "i'm fine",
            "im fine", "iconic", "obsessed", "love this", "best", "amazing", "so good"));

    @Override
    public void onEntryAdded(JournalEntry entry, EmotionResult result) {
        String raw = entry.getRawContent(); // original case preserved
        String text = raw.toLowerCase();

        detectConflict(entry, result, text);
        detectExcessivePunctuation(entry, text);
        detectContradiction(result, text);
        detectAllCaps(entry, raw); // NEW
        detectSobEmojiContext(entry, result, raw, text); // NEW (smart 😭)
    }

    // ── Original checks ───────────────────────────────────────────────────────

    private void detectConflict(JournalEntry entry, EmotionResult result, String text) {
        boolean hasPositiveWord = text.contains("fine") || text.contains("okay")
                || text.contains("good") || text.contains("great");
        // Only flag 😭 as negative here if context is NOT casual (see
        // detectSobEmojiContext)
        boolean hasDefinitelyNegativeEmoji = text.contains("😢") || text.contains("😞")
                || text.contains("😔");
        if (hasPositiveWord && hasDefinitelyNegativeEmoji) {
            detectedAnomalies.add("⚠️ [Entry " + entry.getId()
                    + "] Emotional conflict: positive words with negative emoji — possible masking?");
        }
    }

    private void detectExcessivePunctuation(JournalEntry entry, String text) {
        long exclCount = text.chars().filter(c -> c == '!').count();
        boolean hasSadWord = text.contains("sad") || text.contains("cry")
                || text.contains("hate") || text.contains("done");
        if (exclCount >= 3 && hasSadWord) {
            detectedAnomalies.add("⚠️ [Entry " + entry.getId()
                    + "] Excessive '!!!' with negative words — high emotional intensity detected.");
        }
    }

    private void detectContradiction(EmotionResult result, String text) {
        if (result.getPrimaryEmotion() == Emotion.DEPRESSED && text.contains("happy")) {
            detectedAnomalies.add("⚠️ Contradiction: 'happy' in text but strong depression "
                    + "indicators found. Consider speaking to someone. 💙");
        }
    }

    // ── NEW: All-caps detection ───────────────────────────────────────────────

    /**
     * If more than 60% of alphabetic characters are uppercase AND the entry
     * is at least 10 characters long, flag it as high emotional intensity.
     * e.g. "I AM SO DONE WITH EVERYTHING TODAY" → flagged.
     * Short all-caps like "LOL" or "OMG" are excluded by the length check.
     */
    private void detectAllCaps(JournalEntry entry, String raw) {
        // Strip non-alpha characters first
        String lettersOnly = raw.replaceAll("[^a-zA-Z]", "");
        if (lettersOnly.length() < 10)
            return; // too short to be meaningful

        long upperCount = lettersOnly.chars().filter(Character::isUpperCase).count();
        double capsRatio = (double) upperCount / lettersOnly.length();

        if (capsRatio >= 0.60) {
            detectedAnomalies.add("⚠️ [Entry " + entry.getId()
                    + "] ALL-CAPS detected (" + String.format("%.0f", capsRatio * 100)
                    + "% uppercase) — possible high emotional intensity or distress.");
        }
    }

    // ── NEW: Smart 😭 disambiguation ──────────────────────────────────────────

    /**
     * 😭 (loudly crying face) is one of the most overloaded emojis.
     * People use it for:
     * - Genuine sadness: "I miss them so much 😭"
     * - Humour/irony: "this is so funny I'm dead 😭"
     * - Overwhelming joy: "this is so cute 😭"
     * - Casual hyperbole: "I can't 😭 bro why 😭"
     *
     * Strategy: check surrounding words.
     * - If casual/humour context words found → flag as AMBIGUOUS (not distress)
     * - If genuine sadness words found → flag as potential distress
     * - If 😭 appears 3+ times → likely casual/dramatic, not genuine
     */
    private void detectSobEmojiContext(JournalEntry entry, EmotionResult result,
            String raw, String text) {
        if (!text.contains("😭"))
            return;

        // Count occurrences — 3+ is almost certainly casual usage
        long sobCount = text.chars()
                .filter(c -> c == '\uD83D') // surrogate pair detection for 😭
                .count();
        // Simpler string count approach
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf("😭", idx)) != -1) {
            count++;
            idx++;
        }

        if (count >= 3) {
            detectedAnomalies.add("ℹ️ [Entry " + entry.getId()
                    + "] 😭 used " + count + "x — likely casual/dramatic usage, not genuine distress.");
            return;
        }

        // Check for casual context words
        boolean hasCasualContext = CASUAL_SOB_CONTEXT.stream()
                .anyMatch(text::contains);

        // Check for genuine sadness context
        boolean hasGenuineSadContext = text.contains("miss") || text.contains("lost")
                || text.contains("gone") || text.contains("alone") || text.contains("lonely")
                || text.contains("hurt") || text.contains("pain") || text.contains("broke")
                || text.contains("grief") || text.contains("depress") || text.contains("hopeless");

        if (hasCasualContext && !hasGenuineSadContext) {
            detectedAnomalies.add("ℹ️ [Entry " + entry.getId()
                    + "] 😭 detected but context suggests casual/humorous use — not flagged as distress.");
        } else if (hasGenuineSadContext) {
            detectedAnomalies.add("⚠️ [Entry " + entry.getId()
                    + "] 😭 with sadness context detected — genuine distress possible. Take care 💙");
        }
        // If neither — stay silent (ambiguous, don't over-flag)
    }

    public List<String> getAnomalies() {
        return Collections.unmodifiableList(detectedAnomalies);
    }

    public boolean hasAnomalies() {
        return !detectedAnomalies.isEmpty();
    }

    public void clearAnomalies() {
        detectedAnomalies.clear();
    }

    @Override
    public String getObserverName() {
        return "AnomalyDetector";
    }
}