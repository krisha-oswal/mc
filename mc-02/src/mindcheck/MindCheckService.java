package mindcheck;

import mindcheck.analytics.*;
import mindcheck.emotion.*;
import mindcheck.model.*;
import mindcheck.nlp.*;
import mindcheck.storage.*;
import mindcheck.suggestion.*;
import java.util.*;

/**
 * Central service that wires together all subsystems.
 * Entry point for all business logic.
 */
public class MindCheckService {

    private final EntryRepository repository;
    private final EmotionClassifier classifier;
    private final SuggestionEngine suggestionEngine;
    private final EmotionTimeline<JournalEntry> timeline;
    private final WeeklyReportGenerator reportGenerator;
    private final StreakTracker streakTracker;
    private final AnomalyDetector anomalyDetector;
    private final ProfileManager profileManager;

    public MindCheckService() {
        // Try SQLite first, fall back to in-memory
        EntryRepository repo;
        try {
            repo = new DatabaseEntryRepository();
            System.out.println("  ✅ SQLite database connected (mindcheck.db)");
        } catch (Exception e) {
            repo = new InMemoryEntryRepository();
            System.out.println("  ℹ️  Using in-memory storage (SQLite driver not found)");
        }
        this.repository      = repo;
        this.classifier      = new EmotionClassifier();
        this.suggestionEngine = new SuggestionEngine();
        this.timeline        = new EmotionTimeline<>();
        this.reportGenerator = new WeeklyReportGenerator();
        this.profileManager  = ProfileManager.getInstance();

        // Set up observers
        this.streakTracker   = new StreakTracker();
        this.anomalyDetector = new AnomalyDetector();
        timeline.addObserver(streakTracker);
        timeline.addObserver(anomalyDetector);

        // Load existing entries into timeline for context
        loadExistingEntries();
    }

    private void loadExistingEntries() {
        List<JournalEntry> existing = repository.findRecent(10);
        Collections.reverse(existing);
        for (JournalEntry e : existing) {
            timeline.addEntry(e);
            if (e.getEmotionResult() != null) profileManager.recordEmotion(e.getEmotionResult());
        }
    }

    /**
     * Core method: Process a new journal entry through the full pipeline.
     */
    public AnalysisResult processEntry(JournalEntry entry) {
        // 1. NLP Pipeline (Chain of Responsibility)
        List<EmotionResult> recentResults = timeline.getRecentResults(3);
        NLPPipeline nlp = new NLPPipeline(recentResults);
        ProcessedText pt = nlp.run(entry.getTextForProcessing());

        // 2. Emotion Classification (Hybrid)
        EmotionResult emotionResult = classifier.analyse(pt);
        entry.setEmotionResult(emotionResult);

        // 3. Update profile
        profileManager.recordEmotion(emotionResult);

        // 4. Persist
        repository.save(entry);

        // 5. Add to timeline (triggers observers)
        timeline.addEntry(entry);

        // 6. Generate suggestions (Strategy Pattern)
        Map<String, List<String>> suggestions = suggestionEngine.getAllSuggestions(emotionResult);

        // 7. Check for anomalies
        List<String> anomalies = new ArrayList<>(anomalyDetector.getAnomalies());
        anomalyDetector.clearAnomalies();

        // 8. Check streak milestone
        String milestone = streakTracker.consumeMilestone();

        return new AnalysisResult(entry, emotionResult, pt, suggestions, anomalies, milestone);
    }

    public String getWeeklyReport() {
        return reportGenerator.generateReport(timeline);
    }

    public String getMoodGraph() {
        return reportGenerator.generateMoodGraph(timeline);
    }

    public List<JournalEntry> getAllEntries()       { return repository.findAll(); }
    public List<JournalEntry> getRecentEntries(int n){ return repository.findRecent(n); }
    public int getTotalEntries()                    { return repository.count(); }
    public ProfileManager getProfileManager()       { return profileManager; }
    public StreakTracker getStreakTracker()          { return streakTracker; }

    // ---- Inner result class ----
    public static class AnalysisResult {
        public final JournalEntry entry;
        public final EmotionResult emotionResult;
        public final ProcessedText processedText;
        public final Map<String, List<String>> suggestions;
        public final List<String> anomalies;
        public final String milestoneMesage;

        public AnalysisResult(JournalEntry entry, EmotionResult emotionResult,
                              ProcessedText processedText,
                              Map<String, List<String>> suggestions,
                              List<String> anomalies, String milestone) {
            this.entry          = entry;
            this.emotionResult  = emotionResult;
            this.processedText  = processedText;
            this.suggestions    = suggestions;
            this.anomalies      = anomalies;
            this.milestoneMesage = milestone;
        }
    }
}
