package mindcheck.ui;

import mindcheck.MindCheckService;
import mindcheck.MindCheckService.AnalysisResult;
import mindcheck.model.*;
import mindcheck.emotion.ProfileManager;
import java.util.*;

/**
 * Console-based UI for MindCheck (VS Code / terminal).
 * Replaces the Android UI layer with a rich terminal interface.
 */
public class ConsoleUI {

    private final Scanner scanner;
    private MindCheckService service;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n  Initializing MindCheck systems...");
        service = new MindCheckService();
        System.out.println("  ✅ All systems ready!\n");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> newTextEntry();
                case "2" -> newVoiceEntry();
                case "3" -> viewHistory();
                case "4" -> viewWeeklyReport();
                case "5" -> viewMoodGraph();
                case "6" -> viewProfile();
                case "7" -> runDemo();
                case "8" -> viewNLPDebug();
                case "9" -> viewMoodCalendar();
                case "0" -> {
                    running = false;
                    goodbye();
                }
                default -> System.out.println("  ❌ Invalid choice. Try again.\n");
            }
        }
        scanner.close();
    }

    // ─── MENU ─────────────────────────────────────────────────────────────────

    private void printMainMenu() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       🧠 MindCheck — Smart Journal        ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  1. ✍️   New Text Entry                   ║");
        System.out.println("║  2. 🎙️   New Voice Entry (simulated)      ║");
        System.out.println("║  3. 📖  View Entry History                ║");
        System.out.println("║  4. 📊  Weekly Report                     ║");
        System.out.println("║  5. 📈  Mood Graph                        ║");
        System.out.println("║  6. 👤  My Profile & Insights             ║");
        System.out.println("║  7. 🤖  Run Demo (auto entries)           ║");
        System.out.println("║  8. 🔬  NLP Debug Mode                    ║");
        System.out.println("║  9. 📅  Mood Calendar (7-day)             ║");
        System.out.println("║  0. 👋  Exit                              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.print("  → Choose: ");
    }

    // ─── NEW TEXT ENTRY ───────────────────────────────────────────────────────

    private void newTextEntry() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│  ✍️  New Journal Entry                    │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  Write freely — how are you feeling today?");
        System.out.println("  (emojis supported! Press Enter when done)\n");
        System.out.print("  📝 Your entry: ");

        String content = scanner.nextLine().trim();
        if (content.isEmpty()) {
            System.out.println("  ⚠️  Entry cannot be empty.\n");
            return;
        }

        JournalEntry entry = EntryFactory.createText(content);
        processAndDisplay(entry);
    }

    // ─── NEW VOICE ENTRY ──────────────────────────────────────────────────────

    private void newVoiceEntry() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│  🎙️  Voice Entry (Simulated)             │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  (On Android this would use the Speech API.)");
        System.out.println("  Type what you would say aloud:\n");
        System.out.print("  🎙️  Speak/type: ");

        String content = scanner.nextLine().trim();
        if (content.isEmpty()) {
            System.out.println("  ⚠️  Entry cannot be empty.\n");
            return;
        }

        JournalEntry entry = EntryFactory.createVoice(content);
        System.out.println("  🎙️  Transcribed: \"" + content + "\"");
        processAndDisplay(entry);
    }

    // ─── PROCESS & DISPLAY RESULT ─────────────────────────────────────────────

    private void processAndDisplay(JournalEntry entry) {
        System.out.println("\n  ⚙️  Running NLP Pipeline...");
        System.out.println("  ⚙️  Classifying emotion...\n");

        AnalysisResult result = service.processEntry(entry);
        EmotionResult er = result.emotionResult;

        // ── Emotion Result Banner ──
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.printf("║  EMOTION DETECTED: %-22s║%n", er.getPrimaryEmotion().toString());
        System.out.printf("║  Confidence: %-3.0f%% (%s)%28s║%n",
                er.getConfidence() * 100,
                er.getConfidenceLabel(),
                "");
        System.out.println("╠══════════════════════════════════════════╣");

        // ── Hybrid Score Breakdown ──
        System.out.println("║  📊 Hybrid Score Breakdown:              ║");
        System.out.printf("║    Naive Bayes  (60%%) : %6.2f%%          ║%n", er.getNaiveBayesScore() * 100);
        System.out.printf("║    Rule-Based   (30%%) : %6.2f%%          ║%n", er.getRuleBasedScore() * 100);
        System.out.printf("║    Emoji-Score  (10%%) : %6.2f%%          ║%n", er.getEmojiScore() * 100);
        System.out.println("╠══════════════════════════════════════════╣");

        // ── Valence/Arousal ──
        Emotion emo = er.getPrimaryEmotion();
        System.out.printf("║  Valence : %+.2f  |  Arousal : %+.2f       ║%n",
                emo.getValence(), emo.getArousal());
        System.out.println("╠══════════════════════════════════════════╣");

        // ── NLP Details ──
        System.out.println("║  🔬 NLP Pipeline Output:                 ║");
        System.out.printf("║    Tokens  : %-28s║%n",
                truncate(result.processedText.getTokens().toString(), 28));
        System.out.printf("║    Negation: %-28s║%n",
                result.processedText.isHasNegation() ? "YES ⚠️" : "No");
        System.out.printf("║    Intensity: %-27s║%n",
                String.format("x%.2f", result.processedText.getIntensityMultiplier()));
        System.out.printf("║    Emojis  : %-28s║%n",
                result.processedText.getEmojiEmotionMap().isEmpty()
                        ? "None detected"
                        : truncate(result.processedText.getEmojiEmotionMap().keySet().toString(), 28));
        System.out.println("╚══════════════════════════════════════════╝");

        // ── Anomaly Alerts ──
        if (!result.anomalies.isEmpty()) {
            System.out.println("\n  ⚠️  ANOMALY ALERTS:");
            result.anomalies.forEach(a -> System.out.println("  " + a));
        }

        // ── Streak Milestone ──
        if (result.milestoneMesage != null && !result.milestoneMesage.isEmpty()) {
            System.out.println("\n  " + result.milestoneMesage);
        }

        // ── Suggestions ──
        System.out.println("\n  💡 WELLNESS SUGGESTIONS:");
        System.out.println("  ─────────────────────────────────────────");
        result.suggestions.forEach((strategyName, tips) -> {
            System.out.println("  【" + strategyName + "】");
            tips.forEach(tip -> System.out.println("  " + tip));
            System.out.println();
        });

        System.out.println("  ✅ Entry saved! ID: " + entry.getId());
        System.out.println("  📅 " + entry.getFormattedTimestamp());
        pressEnterToContinue();
    }

    // ─── HISTORY ──────────────────────────────────────────────────────────────

    private void viewHistory() {
        List<JournalEntry> entries = service.getRecentEntries(10);
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│  📖 Recent Journal Entries               │");
        System.out.println("└─────────────────────────────────────────┘");

        if (entries.isEmpty()) {
            System.out.println("  No entries yet. Start journaling!\n");
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            JournalEntry e = entries.get(i);
            String emotionStr = e.getEmotionResult() != null
                    ? e.getEmotionResult().getPrimaryEmotion().toString()
                    : "Not analysed";
            System.out.printf("  %d. [%s] %s%n", i + 1, e.getId(), e.getFormattedTimestamp());
            System.out.printf("     Type: %s | Emotion: %s%n", e.getEntryType(), emotionStr);
            System.out.printf("     \"%s\"%n%n", truncate(e.getRawContent(), 60));
        }

        System.out.print("  Enter entry number to view full text (or 0 to go back): ");
        String input = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < entries.size()) {
                JournalEntry e = entries.get(idx);
                System.out.println("\n  ── Full Entry ──");
                System.out.println("  " + e);
                System.out.println("  Content: " + e.getRawContent());
                if (e.getEmotionResult() != null) {
                    System.out.println("  " + e.getEmotionResult());
                }
            }
        } catch (NumberFormatException ignored) {
        }
        pressEnterToContinue();
    }

    // ─── WEEKLY REPORT ────────────────────────────────────────────────────────

    private void viewWeeklyReport() {
        System.out.println(service.getWeeklyReport());
        pressEnterToContinue();
    }

    // ─── MOOD GRAPH ───────────────────────────────────────────────────────────

    private void viewMoodGraph() {
        System.out.println(service.getMoodGraph());
        pressEnterToContinue();
    }

    // ─── PROFILE ──────────────────────────────────────────────────────────────

    private void viewProfile() {
        ProfileManager pm = service.getProfileManager();
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│  👤 My Mood Profile                      │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.printf("  Total Entries  : %d%n", service.getTotalEntries());
        System.out.printf("  Journal Streak : %d days 🔥%n", pm.getStreak());
        System.out.printf("  Valence Avg    : %.2f (%s)%n",
                pm.getValenceAverage(),
                pm.getValenceAverage() > 0.2 ? "Positive 😊"
                        : pm.getValenceAverage() < -0.2 ? "Negative 😢" : "Neutral 😐");

        System.out.print("  Mood Drift     : ");
        System.out.println(pm.isEmotionDrifting() ? "⚠️  Trending negative" : "✅ Stable");

        System.out.print("  Mood Volatile  : ");
        System.out.println(pm.isMoodVolatile() ? "⚠️  Frequent swings detected" : "✅ Stable");

        List<Emotion> history = pm.getMoodHistory();
        if (!history.isEmpty()) {
            System.out.print("  Emotion History: ");
            history.stream().map(Emotion::getEmoji).forEach(System.out::print);
            System.out.println();
        }

        System.out.println();
        System.out.println("  📌 Add custom trigger word? (y/n): ");
        if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.print("  Word: ");
            String word = scanner.nextLine().trim();
            System.out.print("  Weight (e.g. 1.5 for amplify, 0.5 to dampen): ");
            try {
                double weight = Double.parseDouble(scanner.nextLine().trim());
                pm.addUserWordWeight(word, weight);
                System.out.println("  ✅ Word '" + word + "' added with weight " + weight);
            } catch (NumberFormatException e) {
                System.out.println("  ❌ Invalid weight.");
            }
        }
        pressEnterToContinue();
    }

    // ─── DEMO MODE ────────────────────────────────────────────────────────────

    private void runDemo() {
        System.out.println("\n  🤖 Running Demo — adding sample entries...\n");
        String[] sampleEntries = {
                "I woke up feeling absolutely amazing today! Everything just feels right 😊",
                "Work has been so stressful lately. I'm overwhelmed and can't cope with all these deadlines 😤",
                "Had a really calm evening, went for a walk and felt at peace with everything 😌",
                "Not feeling great. A bit sad and lonely, I miss my friends 😢",
                "I'm fine!!! 😭 Everything is totally okay I promise", // conflict test
                "Super excited about my new project! Can't wait to start 🤩",
                "Feeling very anxious about tomorrow's presentation. What if it all goes wrong?",
                "Today was a good day. Content and satisfied with what I accomplished.",
                "I am not happy with how things turned out. Not at all.", // negation test
                "Feeling calm and grateful. Life is beautiful 🥰"
        };

        for (int i = 0; i < sampleEntries.length; i++) {
            System.out.printf("  [%d/%d] Processing: \"%s\"%n", i + 1, sampleEntries.length,
                    truncate(sampleEntries[i], 55));
            JournalEntry entry = (i % 3 == 0)
                    ? EntryFactory.createVoice(sampleEntries[i])
                    : EntryFactory.createText(sampleEntries[i]);
            AnalysisResult result = service.processEntry(entry);
            System.out.printf("         → %s (%.0f%% confidence)%n%n",
                    result.emotionResult.getPrimaryEmotion(),
                    result.emotionResult.getConfidence() * 100);
        }

        System.out.println("  ✅ Demo complete! Check Weekly Report and Mood Graph for insights.");
        pressEnterToContinue();
    }

    // ─── NLP DEBUG ────────────────────────────────────────────────────────────

    private void viewNLPDebug() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│  🔬 NLP Debug Mode                       │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("  Enter any text to see each NLP pipeline stage:\n");
        System.out.print("  📝 Text: ");
        String text = scanner.nextLine().trim();
        if (text.isEmpty()) {
            System.out.println("  ⚠️  Empty input.");
            return;
        }

        // Run pipeline step by step manually
        mindcheck.model.ProcessedText pt = new mindcheck.model.ProcessedText(text);

        mindcheck.nlp.TextProcessor[] steps = {
                new mindcheck.nlp.Tokenizer(),
                new mindcheck.nlp.EmojiProcessor(),
                new mindcheck.nlp.StopwordFilter(),
                new mindcheck.nlp.PorterStemmer(),
                new mindcheck.nlp.NegationHandler(),
                new mindcheck.nlp.IntensityScorer(),
                new mindcheck.nlp.NGramDetector(),
                new mindcheck.nlp.ContextAnalyzer(new ArrayList<>())
        };

        System.out.println("\n  ── Chain of Responsibility Pipeline ──\n");
        for (mindcheck.nlp.TextProcessor step : steps) {
            pt = step.process(pt);
            System.out.printf("  [%s]%n", step.getStepName());
            System.out.printf("    Tokens    : %s%n", truncate(pt.getTokens().toString(), 60));
            System.out.printf("    Stems     : %s%n", truncate(pt.getStems().toString(), 60));
            System.out.printf("    N-Grams   : %d total%n", pt.getNgrams().size());
            System.out.printf("    Emojis    : %s%n", pt.getEmojiEmotionMap());
            System.out.printf("    Negation  : %b%n", pt.isHasNegation());
            System.out.printf("    Intensity : %.2f%n%n", pt.getIntensityMultiplier());
        }

        // Final classification
        mindcheck.emotion.EmotionClassifier classifier = new mindcheck.emotion.EmotionClassifier();
        EmotionResult result = classifier.analyse(pt);
        System.out.println("  ── Final Classification ──");
        System.out.println("  " + result);
        System.out.println("\n  All Emotion Scores:");
        result.getEmotionScores().entrySet().stream()
                .sorted(Map.Entry.<Emotion, Double>comparingByValue().reversed())
                .forEach(e -> {
                    int bar = (int) (e.getValue() * 30);
                    System.out.printf("  %-10s %s %.4f%n",
                            e.getKey().getLabel(),
                            "█".repeat(Math.max(0, bar)) + "░".repeat(Math.max(0, 30 - bar)),
                            e.getValue());
                });

        pressEnterToContinue();
    }

    // ─── MOOD CALENDAR ───────────────────────────────────────────────────────

    private void viewMoodCalendar() {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│  📅 7-Day Mood Calendar                  │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println(service.getMoodCalendar());
        pressEnterToContinue();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private void pressEnterToContinue() {
        System.out.println("\n  Press Enter to continue...");
        scanner.nextLine();
    }

    private String truncate(String s, int max) {
        if (s == null)
            return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private void goodbye() {
        System.out.println("\n  👋 Thank you for journaling today. Take care of yourself! 💙");
        System.out.println("  ─────────────────────────────────────────────────────────\n");
    }
}