package mindcheck.nlp;

import mindcheck.model.ProcessedText;
import java.util.*;

/**
 * Step 2: Detects emojis in the original text and maps them to emotion scores.
 * Emoji → emotion vector mapping based on standardized emotional semantics.
 */
public class EmojiProcessor implements TextProcessor {

    // Emoji → (emotion label, sentiment score)
    private static final Map<String, double[]> EMOJI_MAP = new LinkedHashMap<>();

    static {
        // Positive emojis → [valence, arousal]
        EMOJI_MAP.put("😊", new double[]{+0.8, +0.5});   // JOYFUL
        EMOJI_MAP.put("😁", new double[]{+0.9, +0.7});   // EXCITED
        EMOJI_MAP.put("🤩", new double[]{+0.9, +0.9});   // EXCITED
        EMOJI_MAP.put("😍", new double[]{+0.8, +0.6});   // JOYFUL
        EMOJI_MAP.put("🥰", new double[]{+0.8, +0.4});   // JOYFUL
        EMOJI_MAP.put("😌", new double[]{+0.3, -0.6});   // CALM
        EMOJI_MAP.put("😀", new double[]{+0.8, +0.6});   // JOYFUL
        EMOJI_MAP.put("😄", new double[]{+0.8, +0.6});   // JOYFUL
        EMOJI_MAP.put("🙂", new double[]{+0.4, +0.1});   // CONTENT
        EMOJI_MAP.put("😎", new double[]{+0.6, +0.3});   // CONTENT
        EMOJI_MAP.put("💪", new double[]{+0.6, +0.8});   // EXCITED
        EMOJI_MAP.put("🎉", new double[]{+0.9, +0.8});   // EXCITED

        // Negative emojis
        EMOJI_MAP.put("😢", new double[]{-0.8, -0.4});   // SAD
        EMOJI_MAP.put("😭", new double[]{-0.9, -0.3});   // SAD (crying loudly)
        EMOJI_MAP.put("😞", new double[]{-0.9, -0.7});   // DEPRESSED
        EMOJI_MAP.put("😔", new double[]{-0.7, -0.5});   // SAD
        EMOJI_MAP.put("😟", new double[]{-0.6, +0.3});   // ANXIOUS
        EMOJI_MAP.put("😰", new double[]{-0.5, +0.8});   // ANXIOUS
        EMOJI_MAP.put("😱", new double[]{-0.7, +0.9});   // ANXIOUS
        EMOJI_MAP.put("😡", new double[]{-0.8, +0.9});   // ANGRY
        EMOJI_MAP.put("🤬", new double[]{-0.9, +0.9});   // ANGRY
        EMOJI_MAP.put("😤", new double[]{-0.5, +0.7});   // STRESSED
        EMOJI_MAP.put("😩", new double[]{-0.6, -0.3});   // STRESSED
        EMOJI_MAP.put("😫", new double[]{-0.7, -0.2});   // STRESSED
        EMOJI_MAP.put("😣", new double[]{-0.6, +0.4});   // ANXIOUS
        EMOJI_MAP.put("🥺", new double[]{-0.5, -0.1});   // SAD

        // Neutral
        EMOJI_MAP.put("😐", new double[]{ 0.0,  0.0});   // NEUTRAL
        EMOJI_MAP.put("🤔", new double[]{ 0.0, +0.2});   // NEUTRAL
        EMOJI_MAP.put("😶", new double[]{ 0.0, -0.2});   // NEUTRAL
    }

    @Override
    public ProcessedText process(ProcessedText input) {
        String original = input.getOriginalText();

        for (Map.Entry<String, double[]> entry : EMOJI_MAP.entrySet()) {
            String emoji = entry.getKey();
            if (original.contains(emoji)) {
                double[] va = entry.getValue();
                // Score = average of valence and dampened arousal
                double score = va[0] * 0.7 + va[1] * 0.3;
                input.addEmojiEmotion(emoji, score);
            }
        }

        return input;
    }

    public static Map<String, double[]> getEmojiMap() { return EMOJI_MAP; }

    @Override
    public String getStepName() { return "EmojiProcessor"; }
}
