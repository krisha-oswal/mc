package mindcheck.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Abstract base class for all journal entries.
 * Demonstrates: Abstraction, Encapsulation
 */
public abstract class JournalEntry {
    private final String id;
    private final LocalDateTime timestamp;
    private String rawContent;
    private EmotionResult emotionResult;

    public JournalEntry(String rawContent) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now();
        this.rawContent = rawContent;
    }

    // Factory constructor for loading from DB
    public JournalEntry(String id, LocalDateTime timestamp, String rawContent) {
        this.id = id;
        this.timestamp = timestamp;
        this.rawContent = rawContent;
    }

    // Abstract method — subclasses must implement
    public abstract String getEntryType();

    // Abstract: get the plain text for NLP
    public abstract String getTextForProcessing();

    // Getters
    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getRawContent() { return rawContent; }
    public EmotionResult getEmotionResult() { return emotionResult; }

    // Setter
    public void setEmotionResult(EmotionResult result) { this.emotionResult = result; }
    protected void setRawContent(String content) { this.rawContent = content; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s Entry #%s @ %s", 
            getEntryType(), "📝", id, getFormattedTimestamp());
    }
}
