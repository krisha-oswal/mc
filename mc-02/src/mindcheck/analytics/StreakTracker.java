package mindcheck.analytics;

import mindcheck.model.*;

/**
 * Observer: Tracks journaling streaks and milestones.
 */
public class StreakTracker implements AnalyticsObserver {

    private int currentStreak = 0;
    private int longestStreak = 0;
    private String lastMilestone = "";

    @Override
    public void onEntryAdded(JournalEntry entry, EmotionResult result) {
        currentStreak++;
        if (currentStreak > longestStreak) longestStreak = currentStreak;

        // Milestone notifications
        if (currentStreak == 3)  lastMilestone = "🎯 3-day streak! You're building a habit!";
        if (currentStreak == 7)  lastMilestone = "🔥 One week streak! Incredible consistency!";
        if (currentStreak == 14) lastMilestone = "💪 Two-week streak! You're a journaling pro!";
        if (currentStreak == 30) lastMilestone = "🏆 30-day streak! Outstanding commitment!";
    }

    public int getCurrentStreak()  { return currentStreak; }
    public int getLongestStreak()  { return longestStreak; }

    public String consumeMilestone() {
        String m = lastMilestone;
        lastMilestone = "";
        return m;
    }

    @Override
    public String getObserverName() { return "StreakTracker"; }
}
