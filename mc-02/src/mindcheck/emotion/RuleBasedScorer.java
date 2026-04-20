package mindcheck.emotion;

import mindcheck.model.*;
import java.util.*;

/**
 * Rule-based scorer using a curated keyword list with direct emotion scores.
 * Contributes 30% to the hybrid final score.
 */
public class RuleBasedScorer {

    // emotion → keyword patterns
    private static final Map<Emotion, List<String>> RULES = new EnumMap<>(Emotion.class);

    static {
        RULES.put(Emotion.JOYFUL,    Arrays.asList("happy","joy","wonderful","love","smile","laugh","great","best","bliss","grateful","thankful","blessed","delight","excited","amazing","fantastic","positive","hope","hopeful","cheerful","glad","elated"));
        RULES.put(Emotion.EXCITED,   Arrays.asList("excited","thrilled","pumped","hyped","wow","incredible","unbelievable","cant wait","can't wait","awesome","stoked","ecstatic","euphoric","overjoyed"));
        RULES.put(Emotion.CALM,      Arrays.asList("calm","peaceful","relaxed","serene","tranquil","quiet","stillness","meditate","breath","breathe","zen","chill","composed","settled","steady"));
        RULES.put(Emotion.CONTENT,   Arrays.asList("content","okay","fine","alright","satisfied","decent","good","manageable","stable","balanced","okay","comfortable","pleasant","acceptable"));
        RULES.put(Emotion.NEUTRAL,   Arrays.asList("neutral","meh","neither","whatever","indifferent","nothing special","average","ordinary","normal","routine"));
        RULES.put(Emotion.ANXIOUS,   Arrays.asList("anxious","anxiety","worry","worried","nervous","scared","fear","panic","dread","uneasy","restless","overthinking","what if","uncertain","doubt","insecure","tense","apprehensive"));
        RULES.put(Emotion.SAD,       Arrays.asList("sad","unhappy","cry","crying","tears","heartbroken","miss","lonely","alone","grief","loss","hurt","pain","sorrow","melancholy","blue","gloomy","down","low","miserable","upset","disappointed"));
        RULES.put(Emotion.ANGRY,     Arrays.asList("angry","mad","furious","rage","hate","irritated","annoyed","frustrated","resentful","bitter","outraged","enraged","hostile","fed up","sick of","cant stand","can't stand"));
        RULES.put(Emotion.STRESSED,  Arrays.asList("stressed","stress","overwhelmed","pressure","burden","deadline","exhausted","tired","burnout","too much","cant cope","can't cope","swamped","juggling","hectic","chaotic","worn out"));
        RULES.put(Emotion.DEPRESSED, Arrays.asList("depressed","depression","hopeless","worthless","empty","numb","lost","broken","pointless","give up","giving up","no point","no reason","dark","darkness","void","hollow","despair","desperate"));
    }

    /**
     * Returns a score per emotion in [0, 1] based on keyword hits.
     */
    public Map<Emotion, Double> score(ProcessedText pt) {
        String fullText = pt.getOriginalText().toLowerCase();
        List<String> tokens = pt.getTokens();
        List<String> stems  = pt.getStems();

        Map<Emotion, Double> scores = new EnumMap<>(Emotion.class);
        for (Emotion e : Emotion.values()) scores.put(e, 0.0);

        for (Map.Entry<Emotion, List<String>> entry : RULES.entrySet()) {
            Emotion emotion = entry.getKey();
            double hits = 0;
            for (String keyword : entry.getValue()) {
                if (fullText.contains(keyword)) hits++;
                // Also check individual tokens & stems
                for (String t : tokens) if (t.equals(keyword)) hits += 0.5;
                for (String s : stems)  if (s.equals(keyword)) hits += 0.5;
            }
            // Normalize: cap at 5 hits = score 1.0
            scores.put(emotion, Math.min(1.0, hits / 5.0));
        }

        return scores;
    }
}
