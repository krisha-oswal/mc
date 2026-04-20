package mindcheck.analytics;

import mindcheck.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates weekly mood summary reports from the emotion timeline.
 */
public class WeeklyReportGenerator {

    public String generateReport(EmotionTimeline<? extends JournalEntry> timeline) {
        List<? extends JournalEntry> entries = timeline.getEntries();
        if (entries.isEmpty()) {
            return "📋 No entries yet. Start journaling to get your weekly report!";
        }

        // Gather emotions
        List<EmotionResult> results = entries.stream()
            .map(JournalEntry::getEmotionResult)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (results.isEmpty()) return "📋 No analysed entries found.";

        // Count emotions
        Map<Emotion, Long> counts = results.stream()
            .collect(Collectors.groupingBy(EmotionResult::getPrimaryEmotion, Collectors.counting()));

        // Average confidence
        double avgConf = results.stream()
            .mapToDouble(EmotionResult::getConfidence)
            .average().orElse(0);

        // Dominant emotion
        Emotion dominant = counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Emotion.NEUTRAL);

        // Average valence
        double avgValence = results.stream()
            .mapToDouble(r -> r.getPrimaryEmotion().getValence())
            .average().orElse(0);

        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════╗\n");
        sb.append("║        📊 WEEKLY MOOD REPORT          ║\n");
        sb.append("╚══════════════════════════════════════╝\n");
        sb.append(String.format("  Total Entries  : %d\n", entries.size()));
        sb.append(String.format("  Dominant Mood  : %s\n", dominant));
        sb.append(String.format("  Avg Confidence : %.0f%%\n", avgConf * 100));
        sb.append(String.format("  Overall Valence: %s (%.2f)\n",
            avgValence > 0.2 ? "Positive 😊" : avgValence < -0.2 ? "Negative 😢" : "Neutral 😐",
            avgValence));

        sb.append("\n  Emotion Breakdown:\n");
        counts.entrySet().stream()
            .sorted(Map.Entry.<Emotion, Long>comparingByValue().reversed())
            .forEach(e -> {
                int bar = (int)(e.getValue() * 10 / entries.size());
                String barStr = "█".repeat(Math.max(1, bar)) + "░".repeat(Math.max(0, 10 - bar));
                sb.append(String.format("  %-10s %s [%d]\n",
                    e.getKey().getLabel(), barStr, e.getValue()));
            });

        // Mood trend arrow
        sb.append("\n  Mood Trend: ");
        if (avgValence > 0.4)       sb.append("↗ Trending Positive 🌟\n");
        else if (avgValence < -0.4) sb.append("↘ Trending Negative — take care of yourself 💙\n");
        else                         sb.append("→ Stable / Mixed 😐\n");

        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }

    /** ASCII bar chart of mood distribution. */
    public String generateMoodGraph(EmotionTimeline<? extends JournalEntry> timeline) {
        List<? extends JournalEntry> recent = timeline.getRecent(10);
        if (recent.isEmpty()) return "No data for graph.";

        StringBuilder sb = new StringBuilder();
        sb.append("\n  📈 Valence Timeline (last ").append(recent.size()).append(" entries):\n");
        sb.append("  +1.0 |");

        for (JournalEntry e : recent) {
            if (e.getEmotionResult() != null) {
                double v = e.getEmotionResult().getPrimaryEmotion().getValence();
                sb.append(v > 0.5 ? " ▲ " : "   ");
            }
        }
        sb.append("\n   0.0 |");
        for (JournalEntry e : recent) {
            if (e.getEmotionResult() != null) {
                double v = e.getEmotionResult().getPrimaryEmotion().getValence();
                sb.append(Math.abs(v) <= 0.3 ? " ─ " : "   ");
            }
        }
        sb.append("\n  -1.0 |");
        for (JournalEntry e : recent) {
            if (e.getEmotionResult() != null) {
                double v = e.getEmotionResult().getPrimaryEmotion().getValence();
                sb.append(v < -0.5 ? " ▼ " : "   ");
            }
        }
        sb.append("\n        ");
        for (int i = 1; i <= recent.size(); i++) sb.append(String.format("E%-2d", i));
        sb.append("\n");
        return sb.toString();
    }
}
