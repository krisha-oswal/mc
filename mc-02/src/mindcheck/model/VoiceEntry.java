package mindcheck.model;

import java.time.LocalDateTime;

/**
 * Simulated voice journal entry (on Android would use Speech API).
 * On desktop: user types the transcribed text.
 * Demonstrates: Inheritance from JournalEntry
 */
public class VoiceEntry extends JournalEntry {
    private final int durationSeconds;

    public VoiceEntry(String transcribedText, int durationSeconds) {
        super(transcribedText);
        this.durationSeconds = durationSeconds;
    }

    public VoiceEntry(String id, LocalDateTime timestamp, String transcribedText, int durationSeconds) {
        super(id, timestamp, transcribedText);
        this.durationSeconds = durationSeconds;
    }

    public int getDurationSeconds() { return durationSeconds; }

    @Override
    public String getEntryType() {
        return "VOICE";
    }

    @Override
    public String getTextForProcessing() {
        // On Android this would be the Speech-to-Text result
        return getRawContent();
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [🎙️ ~%ds]", durationSeconds);
    }
}
