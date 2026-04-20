package mindcheck.storage;

import mindcheck.model.JournalEntry;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory fallback repository (no external dependencies required).
 * Used when SQLite driver is unavailable.
 */
public class InMemoryEntryRepository implements EntryRepository {

    private final LinkedHashMap<String, JournalEntry> store = new LinkedHashMap<>();

    @Override
    public void save(JournalEntry entry) {
        store.put(entry.getId(), entry);
    }

    @Override
    public Optional<JournalEntry> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<JournalEntry> findAll() {
        List<JournalEntry> list = new ArrayList<>(store.values());
        Collections.reverse(list);
        return list;
    }

    @Override
    public List<JournalEntry> findRecent(int limit) {
        List<JournalEntry> all = findAll();
        return all.subList(0, Math.min(limit, all.size()));
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public void clear() {
        store.clear();
    }
}
