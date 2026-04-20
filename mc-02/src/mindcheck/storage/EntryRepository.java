package mindcheck.storage;

import mindcheck.model.JournalEntry;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for journal entry persistence.
 * Demonstrates: Interface / Dependency Inversion
 */
public interface EntryRepository {
    void save(JournalEntry entry);
    Optional<JournalEntry> findById(String id);
    List<JournalEntry> findAll();
    List<JournalEntry> findRecent(int limit);
    void delete(String id);
    int count();
    void clear();
}
