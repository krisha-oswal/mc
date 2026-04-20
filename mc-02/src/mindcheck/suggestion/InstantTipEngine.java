package mindcheck.suggestion;

import mindcheck.model.*;
import java.util.*;

/**
 * Strategy 1: Provides immediate, emotion-specific wellness tips.
 */
public class InstantTipEngine implements MoodSuggestionStrategy {

    private static final Map<Emotion, List<String>> TIPS = new EnumMap<>(Emotion.class);

    static {
        TIPS.put(Emotion.JOYFUL, Arrays.asList(
            "🌟 Amazing! Channel this positive energy into a creative project.",
            "📝 Write down 3 things making you feel this joy — relive it later!",
            "📞 Share your happiness with a friend or family member.",
            "🎯 Great time to tackle a challenging goal — you're in the zone!"
        ));
        TIPS.put(Emotion.EXCITED, Arrays.asList(
            "🚀 Harness this excitement! Set a specific goal to work toward.",
            "🧘 Take a few deep breaths to ground this excited energy productively.",
            "📋 Make a quick action list while motivation is high.",
            "💬 Share your excitement with someone who'll celebrate with you."
        ));
        TIPS.put(Emotion.CALM, Arrays.asList(
            "🌿 Enjoy this peaceful state — maybe a short walk in nature?",
            "📚 This is a perfect time for reading or learning something new.",
            "🧘 Try a 5-minute mindfulness meditation to deepen the calm.",
            "✍️  Use this clarity to plan something important."
        ));
        TIPS.put(Emotion.CONTENT, Arrays.asList(
            "😊 Contentment is underrated — appreciate what you have today.",
            "🙏 Practice gratitude: list 3 things going well in your life.",
            "🌱 Use this stable energy to nurture a habit you've been building.",
            "💌 Send a kind message to someone you appreciate."
        ));
        TIPS.put(Emotion.NEUTRAL, Arrays.asList(
            "🎲 Neutral days are great for trying something new or spontaneous!",
            "🏃 A brisk 15-minute walk can shift your energy positively.",
            "📖 Pick up that book or podcast you've been meaning to start.",
            "🍵 Make yourself a warm drink and sit quietly for a few minutes."
        ));
        TIPS.put(Emotion.ANXIOUS, Arrays.asList(
            "🌬️  Try box breathing: inhale 4s → hold 4s → exhale 4s → hold 4s.",
            "📝 Write down exactly what's worrying you — getting it out helps.",
            "🚶 A 10-minute walk can significantly reduce anxiety levels.",
            "📞 Reach out to someone you trust and talk through your worries.",
            "⏰  Focus only on what you can control right now, not the future."
        ));
        TIPS.put(Emotion.SAD, Arrays.asList(
            "💙 It's okay to feel sad — allow yourself to feel it without judgment.",
            "🤗 Reach out to a friend or loved one — connection heals.",
            "🛁  Take a warm shower or bath — physical comfort helps emotional pain.",
            "🎵 Listen to music that matches or gently lifts your mood.",
            "🌅 Get some sunlight — even 10 minutes outside can help."
        ));
        TIPS.put(Emotion.ANGRY, Arrays.asList(
            "💨 Take 10 slow, deep breaths before doing or saying anything.",
            "🏋️  Physical exercise is a healthy outlet — try a run or workout.",
            "📝 Write out your anger in a journal — don't hold it inside.",
            "⏳  Give yourself space: wait 20 minutes before responding to anyone.",
            "🔎 Ask yourself: will this matter in 5 years? What do you actually need?"
        ));
        TIPS.put(Emotion.STRESSED, Arrays.asList(
            "📋 Make a priority list — pick ONE thing to focus on right now.",
            "⏸️  Take a proper break: step away from screens for 15 minutes.",
            "🧘 Try progressive muscle relaxation — tense and release each body part.",
            "💤 Check your sleep — stress and poor sleep create a vicious cycle.",
            "🗣️  Talk to someone — stress shared is stress halved."
        ));
        TIPS.put(Emotion.DEPRESSED, Arrays.asList(
            "💙 You're not alone. Please consider talking to a mental health professional.",
            "🌞 Try to get outside for at least 10 minutes of natural light today.",
            "🤝 Reach out to one person you trust, even just to say hello.",
            "🐾 Small actions matter: drink water, eat something, move a little.",
            "📞 If you're struggling, a counselor or helpline can be a great support."
        ));
    }

    @Override
    public List<String> getSuggestions(EmotionResult result) {
        Emotion e = result.getPrimaryEmotion();
        List<String> all = TIPS.getOrDefault(e, Collections.singletonList("Take care of yourself today. 💙"));
        // Return top 2-3 randomly shuffled
        List<String> copy = new ArrayList<>(all);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(3, copy.size()));
    }

    @Override
    public String getStrategyName() { return "Instant Tips"; }
}
