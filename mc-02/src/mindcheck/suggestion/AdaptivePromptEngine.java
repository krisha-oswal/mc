package mindcheck.suggestion;

import mindcheck.emotion.ProfileManager;
import mindcheck.model.*;
import java.time.LocalTime;
import java.util.*;

/**
 * Strategy 3: Adaptive prompts based on time of day, entry count, and emotion confidence.
 */
public class AdaptivePromptEngine implements MoodSuggestionStrategy {

    @Override
    public List<String> getSuggestions(EmotionResult result) {
        List<String> prompts = new ArrayList<>();
        ProfileManager pm = ProfileManager.getInstance();
        LocalTime now = LocalTime.now();

        // Time-based recommendations
        if (now.isBefore(LocalTime.of(9, 0))) {
            prompts.add("🌅 Morning entry! Set an intention for today: what's one thing you want to feel by evening?");
        } else if (now.isBefore(LocalTime.of(12, 0))) {
            prompts.add("☀️ Mid-morning check-in — how is your energy level compared to how you feel emotionally?");
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            prompts.add("🌤️ Afternoon journal — what's been the highlight of your day so far?");
        } else if (now.isBefore(LocalTime.of(21, 0))) {
            prompts.add("🌆 Evening reflection — what went well today, even if it was a tough day?");
        } else {
            prompts.add("🌙 Late-night journaling — try a quick body scan: where are you holding tension?");
        }

        // Low confidence — ask for more detail
        if (result.getConfidence() < 0.5) {
            prompts.add("🤔 Your entry was brief — try expanding: what specifically triggered this feeling?");
        }

        // First-time user encouragement
        if (pm.getTotalEntries() <= 3) {
            prompts.add("👋 You're just getting started! The more you journal, the more personalized your insights become.");
        }

        // High confidence positive — reinforce
        if (result.getConfidence() >= 0.8 && result.getPrimaryEmotion().isPositive()) {
            prompts.add("💡 What specific action or thought contributed to feeling " + result.getPrimaryEmotion().getLabel() + " today? Note it for future reference!");
        }

        return prompts;
    }

    @Override
    public String getStrategyName() { return "Adaptive Prompts"; }
}
