package mindcheck.analytics;

import mindcheck.model.*;
import java.util.*;

/**
 * Generic timeline that holds journal entries and notifies observers.
 * Demonstrates: Generics (T extends JournalEntry), Observer Pattern (Subject side)
 */
public class EmotionTimeline<T extends JournalEntry> {

    private final List<T> entries;
    private final List<AnalyticsObserver> observers;

    public EmotionTimeline() {
        this.entries   = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public void addObserver(AnalyticsObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AnalyticsObserver observer) {
        observers.remove(observer);
    }

    /**
     * Add entry and notify all observers.
     */
    public void addEntry(T entry) {
        entries.add(entry);
        notifyObservers(entry, entry.getEmotionResult());
    }

    private void notifyObservers(T entry, EmotionResult result) {
        for (AnalyticsObserver obs : observers) {
            obs.onEntryAdded(entry, result);
        }
    }

    public List<T> getEntries()          { return Collections.unmodifiableList(entries); }
    public int size()                    { return entries.size(); }

    /** Get entries from last N entries. */
    public List<T> getRecent(int n) {
        int size = entries.size();
        return new ArrayList<>(entries.subList(Math.max(0, size - n), size));
    }

    /** Get emotion results for context analysis. */
    public List<EmotionResult> getRecentResults(int n) {
        List<EmotionResult> results = new ArrayList<>();
        for (T entry : getRecent(n)) {
            if (entry.getEmotionResult() != null) results.add(entry.getEmotionResult());
        }
        return results;
    }
}
