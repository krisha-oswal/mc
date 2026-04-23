package mindcheck.analytics;

import mindcheck.model.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a 7-day ASCII mood calendar from journal entries.
 *
 * Each day shows:
 * - Day name (Mon, Tue...)
 * - Dominant emotion emoji for that day
 * - Entry count
 * - A mini valence bar
 *
 * Example output:
 * ╔═══════════════════════════════════════════════╗
 * ║ 📅 7-DAY MOOD CALENDAR ║
 * ╠═══════════════════════════════════════════════╣
 * ║ Mon Tue Wed Thu Fri Sat Sun ║
 * ║ 😊 😤 😌 😢 😐 🤩 😌 ║
 * ║ [2] [1] [1] [3] [0] [1] [2] ║
 * ║ +++ -- + --- · +++ + ║
 * ╚═══════════════════════════════════════════════╝
 */
public class MoodCalendar {

    /**
     * Generates the 7-day mood calendar string.
     * Looks back 7 days from today and groups entries by date.
     */
    public String generateCalendar(EmotionTimeline<? extends JournalEntry> timeline) {
        List<? extends JournalEntry> allEntries = timeline.getEntries();

        // Build a map: LocalDate → List of entries for that day
        Map<LocalDate, List<JournalEntry>> byDay = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Pre-fill all 7 days in order (oldest → newest) so empty days show too
        for (int i = 6; i >= 0; i--) {
            byDay.put(today.minusDays(i), new ArrayList<>());
        }

        // Group entries into their day buckets
        for (JournalEntry entry : allEntries) {
            LocalDate entryDate = entry.getTimestamp().toLocalDate();
            if (byDay.containsKey(entryDate)) {
                byDay.get(entryDate).add(entry);
            }
        }

        // ── Build output ──────────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔═══════════════════════════════════════════════════╗\n");
        sb.append("║            📅  7-DAY MOOD CALENDAR                ║\n");
        sb.append("╠═══════════════════════════════════════════════════╣\n");

        // Row 1: Day names
        sb.append("║  ");
        for (LocalDate date : byDay.keySet()) {
            String dayName = date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            sb.append(String.format("%-7s", dayName));
        }
        sb.append("║\n");

        // Row 2: Emotion emojis
        sb.append("║  ");
        for (Map.Entry<LocalDate, List<JournalEntry>> dayEntry : byDay.entrySet()) {
            List<JournalEntry> entries = dayEntry.getValue();
            String emoji = getDominantEmoji(entries);
            sb.append(String.format("%-7s", emoji));
        }
        sb.append("║\n");

        // Row 3: Entry counts
        sb.append("║  ");
        for (Map.Entry<LocalDate, List<JournalEntry>> dayEntry : byDay.entrySet()) {
            int count = dayEntry.getValue().size();
            String label = count == 0 ? "·" : "[" + count + "]";
            sb.append(String.format("%-7s", label));
        }
        sb.append("║\n");

        // Row 4: Valence bar (+ positive, - negative, · neutral/none)
        sb.append("║  ");
        for (Map.Entry<LocalDate, List<JournalEntry>> dayEntry : byDay.entrySet()) {
            String bar = getValenceBar(dayEntry.getValue());
            sb.append(String.format("%-7s", bar));
        }
        sb.append("║\n");

        sb.append("╠═══════════════════════════════════════════════════╣\n");

        // Summary row
        long activeDays = byDay.values().stream().filter(l -> !l.isEmpty()).count();
        long totalEntries = byDay.values().stream().mapToLong(List::size).sum();
        OptionalDouble avgValence = byDay.values().stream()
                .flatMap(List::stream)
                .filter(e -> e.getEmotionResult() != null)
                .mapToDouble(e -> e.getEmotionResult().getPrimaryEmotion().getValence())
                .average();

        sb.append(String.format("║  Active days: %d/7  |  Entries: %d  |  Avg mood: %s%s║\n",
                activeDays,
                totalEntries,
                avgValence.isPresent()
                        ? (avgValence.getAsDouble() > 0.2 ? "Positive 😊"
                                : avgValence.getAsDouble() < -0.2 ? "Negative 😢"
                                        : "Neutral 😐")
                        : "No data",
                "         ".substring(0, Math.max(0, 9 - (avgValence.isPresent() ? 11 : 7)))));

        sb.append("╚═══════════════════════════════════════════════════╝\n");

        // Add a helpful note if there are no entries at all
        if (totalEntries == 0) {
            return "\n  📅 No entries in the last 7 days. Start journaling to see your mood calendar!\n";
        }

        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the emoji of the dominant emotion for a day's entries.
     * If no entries, returns a grey dot.
     */
    private String getDominantEmoji(List<JournalEntry> entries) {
        if (entries.isEmpty())
            return "·";

        // Count emotions and find the most frequent
        Map<Emotion, Long> counts = entries.stream()
                .filter(e -> e.getEmotionResult() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getEmotionResult().getPrimaryEmotion(),
                        Collectors.counting()));

        if (counts.isEmpty())
            return "·";

        Emotion dominant = counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Emotion.NEUTRAL);

        return dominant.getEmoji();
    }

    /**
     * Returns a valence bar string for a day.
     * Uses + for positive, - for negative, · for neutral/none.
     * Bar length reflects average valence magnitude (1-3 chars).
     */
    private String getValenceBar(List<JournalEntry> entries) {
        if (entries.isEmpty())
            return "·";

        OptionalDouble avgValence = entries.stream()
                .filter(e -> e.getEmotionResult() != null)
                .mapToDouble(e -> e.getEmotionResult().getPrimaryEmotion().getValence())
                .average();

        if (!avgValence.isPresent())
            return "·";

        double v = avgValence.getAsDouble();
        int bars = (int) Math.round(Math.abs(v) * 3); // 0-3 bars
        bars = Math.max(1, bars);

        if (v > 0.1)
            return "+".repeat(bars);
        if (v < -0.1)
            return "-".repeat(bars);
        return "·";
    }
}
