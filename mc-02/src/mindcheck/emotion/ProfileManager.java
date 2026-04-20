package mindcheck.emotion;

import mindcheck.model.*;
import java.util.*;

/**
 * Singleton user profile manager.
 * Stores user-specific vocabulary, weights, mood history, and streaks.
 * Demonstrates: Singleton Pattern
 */
public class ProfileManager {

    private static ProfileManager instance;

    private final Map<String, Double> userWordWeights;  // word → custom weight
    private final List<String> triggerWords;
    private final List<Emotion> moodHistory;            // ordered list
    private int streak;                                  // consecutive journaling days
    private int totalEntries;

    // Emotion drift: tracks if mood is shifting over time
    private double currentValenceAverage;

    private ProfileManager() {
        this.userWordWeights    = new HashMap<>();
        this.triggerWords       = new ArrayList<>();
        this.moodHistory        = new ArrayList<>();
        this.streak             = 0;
        this.totalEntries       = 0;
        this.currentValenceAverage = 0.0;
    }

    /** Returns the single instance. Thread-safe via synchronized. */
    public static synchronized ProfileManager getInstance() {
        if (instance == null) instance = new ProfileManager();
        return instance;
    }

    /** Record a new emotion result (called after each analysis). */
    public void recordEmotion(EmotionResult result) {
        moodHistory.add(result.getPrimaryEmotion());
        totalEntries++;
        streak++;
        updateValenceAverage(result.getPrimaryEmotion().getValence());
    }

    /** Returns last N emotion results (for ContextAnalyzer). */
    public List<Emotion> getRecentEmotions(int n) {
        int size = moodHistory.size();
        if (size == 0) return new ArrayList<>();
        return new ArrayList<>(moodHistory.subList(Math.max(0, size - n), size));
    }

    /** Add or update a user-specific word weight. */
    public void addUserWordWeight(String word, double weight) {
        userWordWeights.put(word.toLowerCase(), weight);
    }

    /** Get weight for a word (default 1.0 if not set). */
    public double getWordWeight(String word) {
        return userWordWeights.getOrDefault(word.toLowerCase(), 1.0);
    }

    public void addTriggerWord(String word) { triggerWords.add(word.toLowerCase()); }
    public boolean isTriggerWord(String word){ return triggerWords.contains(word.toLowerCase()); }

    public int getStreak()        { return streak; }
    public int getTotalEntries()  { return totalEntries; }
    public List<Emotion> getMoodHistory() { return Collections.unmodifiableList(moodHistory); }
    public double getValenceAverage()     { return currentValenceAverage; }

    /** Detect if mood is significantly drifting negative. */
    public boolean isEmotionDrifting() {
        if (moodHistory.size() < 3) return false;
        List<Emotion> recent = getRecentEmotions(5);
        long negCount = recent.stream().filter(Emotion::isNegative).count();
        return negCount >= 3;
    }

    /** Detect mood volatility (frequent swings). */
    public boolean isMoodVolatile() {
        if (moodHistory.size() < 4) return false;
        List<Emotion> recent = getRecentEmotions(6);
        int swings = 0;
        for (int i = 1; i < recent.size(); i++) {
            boolean prevPos = recent.get(i-1).isPositive();
            boolean currPos = recent.get(i).isPositive();
            if (prevPos != currPos) swings++;
        }
        return swings >= 3;
    }

    private void updateValenceAverage(double newValence) {
        if (totalEntries == 1) currentValenceAverage = newValence;
        else currentValenceAverage = currentValenceAverage * 0.8 + newValence * 0.2; // EMA
    }

    /** Reset (for testing). */
    public static void reset() { instance = null; }
}
