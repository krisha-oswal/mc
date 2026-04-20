package mindcheck.model;

import java.time.LocalDateTime;

/**
 * Concrete text journal entry.
 * Demonstrates: Inheritance from JournalEntry
 */
public class TextEntry extends JournalEntry {

    public TextEntry(String rawContent) {
        super(rawContent);
    }

    public TextEntry(String id, LocalDateTime timestamp, String rawContent) {
        super(id, timestamp, rawContent);
    }

    @Override
    public String getEntryType() {
        return "TEXT";
    }

    @Override
    public String getTextForProcessing() {
        return getRawContent();
    }
}
