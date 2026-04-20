package mindcheck.storage;

import mindcheck.model.*;
import mindcheck.security.AESEncryptor;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQLite-backed implementation of EntryRepository.
 * Uses JDBC (bundled SQLite driver via pure Java).
 * Stores encrypted journal text.
 * Demonstrates: Repository Pattern, Encapsulation, AES encryption
 */
public class DatabaseEntryRepository implements EntryRepository {

    private static final String DB_FILE = "mindcheck.db";
    private final Connection conn;
    private final AESEncryptor encryptor;

    public DatabaseEntryRepository() throws Exception {
        // Load SQLite JDBC driver
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        encryptor = new AESEncryptor();
        initTables();
    }

    private void initTables() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS entries (
                id          TEXT PRIMARY KEY,
                type        TEXT NOT NULL,
                content     TEXT NOT NULL,
                timestamp   TEXT NOT NULL,
                emotion     TEXT,
                confidence  REAL,
                duration    INTEGER DEFAULT 0
            );
            CREATE TABLE IF NOT EXISTS emotion_data (
                entry_id    TEXT PRIMARY KEY,
                nb_score    REAL,
                rule_score  REAL,
                emoji_score REAL,
                FOREIGN KEY(entry_id) REFERENCES entries(id)
            );
            CREATE TABLE IF NOT EXISTS user_profile (
                key         TEXT PRIMARY KEY,
                value       TEXT NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_entries_timestamp ON entries(timestamp);
            """;
        try (Statement stmt = conn.createStatement()) {
            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) stmt.execute(s.trim());
            }
        }
    }

    @Override
    public void save(JournalEntry entry) {
        String encryptedContent = encryptor.encrypt(entry.getRawContent());
        String emotion = null;
        double confidence = 0;
        if (entry.getEmotionResult() != null) {
            emotion = entry.getEmotionResult().getPrimaryEmotion().name();
            confidence = entry.getEmotionResult().getConfidence();
        }

        String sql = "INSERT OR REPLACE INTO entries (id, type, content, timestamp, emotion, confidence, duration) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getId());
            ps.setString(2, entry.getEntryType());
            ps.setString(3, encryptedContent);
            ps.setString(4, entry.getTimestamp().toString());
            ps.setString(5, emotion);
            ps.setDouble(6, confidence);
            ps.setInt(7, entry instanceof VoiceEntry ? ((VoiceEntry)entry).getDurationSeconds() : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB save error: " + e.getMessage());
        }

        // Save emotion detail
        if (entry.getEmotionResult() != null) {
            saveEmotionData(entry.getId(), entry.getEmotionResult());
        }
    }

    private void saveEmotionData(String entryId, EmotionResult result) {
        String sql = "INSERT OR REPLACE INTO emotion_data (entry_id, nb_score, rule_score, emoji_score) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entryId);
            ps.setDouble(2, result.getNaiveBayesScore());
            ps.setDouble(3, result.getRuleBasedScore());
            ps.setDouble(4, result.getEmojiScore());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB emotion_data error: " + e.getMessage());
        }
    }

    @Override
    public Optional<JournalEntry> findById(String id) {
        String sql = "SELECT * FROM entries WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(rowToEntry(rs));
        } catch (SQLException e) {
            System.err.println("DB findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<JournalEntry> findAll() {
        List<JournalEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM entries ORDER BY timestamp DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rowToEntry(rs));
        } catch (SQLException e) {
            System.err.println("DB findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<JournalEntry> findRecent(int limit) {
        List<JournalEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM entries ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rowToEntry(rs));
        } catch (SQLException e) {
            System.err.println("DB findRecent error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void delete(String id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM entries WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DB delete error: " + e.getMessage());
        }
    }

    @Override
    public int count() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM entries")) {
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }

    @Override
    public void clear() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM entries");
            stmt.execute("DELETE FROM emotion_data");
        } catch (SQLException e) {
            System.err.println("DB clear error: " + e.getMessage());
        }
    }

    private JournalEntry rowToEntry(ResultSet rs) throws SQLException {
        String id      = rs.getString("id");
        String type    = rs.getString("type");
        String content = encryptor.decrypt(rs.getString("content"));
        LocalDateTime ts = LocalDateTime.parse(rs.getString("timestamp"));
        int duration   = rs.getInt("duration");
        String emotion = rs.getString("emotion");
        double conf    = rs.getDouble("confidence");

        JournalEntry entry;
        if ("VOICE".equals(type)) {
            entry = new VoiceEntry(id, ts, content, duration);
        } else {
            entry = new TextEntry(id, ts, content);
        }

        // Reconstruct minimal EmotionResult if available
        if (emotion != null && !emotion.isEmpty()) {
            try {
                Emotion e = Emotion.valueOf(emotion);
                Map<Emotion, Double> scores = new EnumMap<>(Emotion.class);
                scores.put(e, conf);
                EmotionResult result = new EmotionResult(e, conf, 0, 0, 0, scores);
                entry.setEmotionResult(result);
            } catch (IllegalArgumentException ignored) {}
        }

        return entry;
    }

    public void close() {
        try { if (conn != null) conn.close(); }
        catch (SQLException e) { /* ignore */ }
    }
}
