package mindcheck.model;

/**
 * Factory class for creating JournalEntry objects.
 * Demonstrates: Factory Pattern
 */
public class EntryFactory {

    public enum EntryType {
        TEXT, VOICE
    }

    public static JournalEntry create(EntryType type, String content) {
        switch (type) {
            case TEXT:
                return new TextEntry(content);
            case VOICE:
                // Simulate ~1 word per second for "recording duration"
                int estimatedDuration = Math.max(5, content.split("\\s+").length);
                return new VoiceEntry(content, estimatedDuration);
            default:
                throw new IllegalArgumentException("Unknown entry type: " + type);
        }
    }

    public static JournalEntry createText(String content) {
        return create(EntryType.TEXT, content);
    }

    public static JournalEntry createVoice(String transcribedContent) {
        return create(EntryType.VOICE, transcribedContent);
    }
}
