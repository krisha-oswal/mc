package mindcheck.suggestion;

import mindcheck.emotion.ProfileManager;
import mindcheck.model.*;
import java.util.*;

/**
 * Strategy 2: Analyses mood history patterns and gives longer-term suggestions.
 */
public class PatternSuggestionEngine implements MoodSuggestionStrategy {

    @Override
    public List<String> getSuggestions(EmotionResult result) {
        ProfileManager pm = ProfileManager.getInstance();
        List<String> suggestions = new ArrayList<>();

        // Drifting negative
        if (pm.isEmotionDrifting()) {
            suggestions.add("📉 Your mood has been trending negative lately. Consider scheduling something you enjoy this week.");
            suggestions.add("🧑‍⚕️ If this negative trend continues, speaking with a counselor or therapist can be very helpful.");
        }

        // Mood volatility
        if (pm.isMoodVolatile()) {
            suggestions.add("📊 Your mood has been fluctuating a lot. Try adding structure and routine to your day.");
            suggestions.add("😴 Check your sleep quality — mood swings are often linked to poor rest.");
        }

        // Streak encouragement
        int streak = pm.getStreak();
        if (streak >= 7) {
            suggestions.add("🔥 " + streak + "-day journaling streak! Consistency is building self-awareness — keep going!");
        } else if (streak >= 3) {
            suggestions.add("✨ " + streak + " days journaling in a row — you're building a great habit!");
        }

        // Valence-based patterns
        double val = pm.getValenceAverage();
        if (val < -0.5) {
            suggestions.add("💙 Your overall mood has been quite low. Small positive actions each day can help shift the trend.");
        } else if (val > 0.5) {
            suggestions.add("🌟 Your overall mood has been very positive! Keep doing what's working.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("📊 Keep journaling — patterns will emerge over time to give you deeper insights.");
        }

        return suggestions;
    }

    @Override
    public String getStrategyName() { return "Pattern Analysis"; }
}
