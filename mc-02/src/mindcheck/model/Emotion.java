package mindcheck.model;

/**
 * Emotion labels based on Valence-Arousal psychological model.
 * Each emotion has a valence (positive/negative) and arousal (energy level).
 */
public enum Emotion {
    //           Label         Valence  Arousal  Emoji
    JOYFUL      ("Joyful",     +0.8,   +0.7,    "😊"),
    EXCITED     ("Excited",    +0.7,   +0.9,    "🤩"),
    CALM        ("Calm",       +0.3,   -0.6,    "😌"),
    CONTENT     ("Content",    +0.5,   -0.2,    "🙂"),
    NEUTRAL     ("Neutral",     0.0,    0.0,    "😐"),
    ANXIOUS     ("Anxious",    -0.4,   +0.8,    "😰"),
    SAD         ("Sad",        -0.8,   -0.5,    "😢"),
    ANGRY       ("Angry",      -0.7,   +0.9,    "😡"),
    STRESSED    ("Stressed",   -0.5,   +0.7,    "😤"),
    DEPRESSED   ("Depressed",  -0.9,   -0.8,    "😞");

    private final String label;
    private final double valence;
    private final double arousal;
    private final String emoji;

    Emotion(String label, double valence, double arousal, String emoji) {
        this.label = label;
        this.valence = valence;
        this.arousal = arousal;
        this.emoji = emoji;
    }

    public String getLabel()   { return label; }
    public double getValence() { return valence; }
    public double getArousal() { return arousal; }
    public String getEmoji()   { return emoji; }

    public boolean isPositive() { return valence > 0.1; }
    public boolean isNegative() { return valence < -0.1; }

    @Override
    public String toString() { return emoji + " " + label; }
}
