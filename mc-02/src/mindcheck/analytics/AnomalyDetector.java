package mindcheck.analytics;

import mindcheck.model.*;
import java.util.*;

/**
 * Observer: Detects emotional anomalies and conflicts.
 * Example: "I'm fine!!! 😭" → flagged as conflict.
 */
public class AnomalyDetector implements AnalyticsObserver {

    private final List<String> detectedAnomalies = new ArrayList<>();

    @Override
    public void onEntryAdded(JournalEntry entry, EmotionResult result) {
        String text = entry.getRawContent().toLowerCase();

        // Detect sarcasm/conflict markers
        detectConflict(entry, result, text);
        detectExcessivePunctuation(entry, text);
        detectContradiction(result, text);
    }

    private void detectConflict(JournalEntry entry, EmotionResult result, String text) {
        // Positive words + negative emoji = conflict
        boolean hasPositiveWord = text.contains("fine") || text.contains("okay") || text.contains("good");
        boolean hasNegativeEmoji = text.contains("😭") || text.contains("😢") || text.contains("😞");

        if (hasPositiveWord && hasNegativeEmoji) {
            detectedAnomalies.add("⚠️ [Entry " + entry.getId() + "] Emotional conflict detected: positive words with negative emoji — possible masking?");
        }
    }

    private void detectExcessivePunctuation(JournalEntry entry, String text) {
        long exclCount = text.chars().filter(c -> c == '!').count();
        boolean hasSadEmotion = text.contains("😭") || text.contains("sad") || text.contains("cry");
        if (exclCount >= 3 && hasSadEmotion) {
            detectedAnomalies.add("⚠️ [Entry " + entry.getId() + "] Excessive '!!!' with negative emotion — emotional distress detected.");
        }
    }

    private void detectContradiction(EmotionResult result, String text) {
        // "I'm so happy" but classified as DEPRESSED
        if (result.getPrimaryEmotion() == Emotion.DEPRESSED && text.contains("happy")) {
            detectedAnomalies.add("⚠️ Contradiction: 'happy' in text but strong depression indicators found. Consider speaking to someone.");
        }
    }

    public List<String> getAnomalies()     { return Collections.unmodifiableList(detectedAnomalies); }
    public boolean hasAnomalies()          { return !detectedAnomalies.isEmpty(); }
    public void clearAnomalies()           { detectedAnomalies.clear(); }

    @Override
    public String getObserverName() { return "AnomalyDetector"; }
}
