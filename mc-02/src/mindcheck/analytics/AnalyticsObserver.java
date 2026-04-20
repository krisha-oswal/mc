package mindcheck.analytics;

import mindcheck.model.*;

/**
 * Observer interface for the analytics event system.
 * Demonstrates: Observer Pattern
 */
public interface AnalyticsObserver {
    void onEntryAdded(JournalEntry entry, EmotionResult result);
    String getObserverName();
}
