# 🧠 MindCheck — AI-Powered Smart Journaling App

A complete Java implementation of the MindCheck intelligent journaling system,
fully runnable in VS Code as a console application.

---

## 📁 Project Structure

```
MindCheck/
├── src/mindcheck/
│   ├── Main.java                     ← Entry point
│   ├── MindCheckService.java         ← Core service orchestrator
│   ├── model/
│   │   ├── JournalEntry.java         ← Abstract base (Abstraction)
│   │   ├── TextEntry.java            ← Text entry (Inheritance)
│   │   ├── VoiceEntry.java           ← Voice entry (Inheritance)
│   │   ├── EntryFactory.java         ← Factory Pattern
│   │   ├── Emotion.java              ← Valence-Arousal emotion enum
│   │   ├── EmotionResult.java        ← Classification result
│   │   └── ProcessedText.java        ← NLP pipeline data carrier
│   ├── nlp/
│   │   ├── TextProcessor.java        ← Chain of Responsibility interface
│   │   ├── Tokenizer.java            ← Step 1: Tokenize
│   │   ├── EmojiProcessor.java       ← Step 2: Emoji → emotion vector
│   │   ├── StopwordFilter.java       ← Step 3: Remove stopwords
│   │   ├── PorterStemmer.java        ← Step 4: Custom stemming
│   │   ├── NegationHandler.java      ← Step 5: "not happy" detection
│   │   ├── IntensityScorer.java      ← Step 6: "very", "extremely"
│   │   ├── NGramDetector.java        ← Step 7: Bigrams/Trigrams
│   │   ├── ContextAnalyzer.java      ← Step 8: History-aware context
│   │   └── NLPPipeline.java          ← Pipeline orchestrator
│   ├── emotion/
│   │   ├── MoodAnalyser.java         ← Interface (Polymorphism)
│   │   ├── NaiveBayesClassifier.java ← Custom Naive Bayes
│   │   ├── RuleBasedScorer.java      ← Keyword rule engine
│   │   ├── EmotionClassifier.java    ← Hybrid model (NB+Rules+Emoji)
│   │   └── ProfileManager.java       ← Singleton: user profile
│   ├── suggestion/
│   │   ├── MoodSuggestionStrategy.java ← Strategy Pattern interface
│   │   ├── InstantTipEngine.java       ← Strategy 1: Immediate tips
│   │   ├── PatternSuggestionEngine.java← Strategy 2: Pattern insights
│   │   ├── AdaptivePromptEngine.java   ← Strategy 3: Time-aware prompts
│   │   └── SuggestionEngine.java       ← Strategy orchestrator
│   ├── analytics/
│   │   ├── AnalyticsObserver.java    ← Observer Pattern interface
│   │   ├── EmotionTimeline.java      ← Generic timeline (Generics)
│   │   ├── StreakTracker.java        ← Observer: streak tracking
│   │   ├── AnomalyDetector.java      ← Observer: conflict detection
│   │   └── WeeklyReportGenerator.java← Report + ASCII graph
│   ├── security/
│   │   └── AESEncryptor.java         ← AES-128 CBC encryption
│   ├── storage/
│   │   ├── EntryRepository.java      ← Repository interface
│   │   ├── DatabaseEntryRepository.java ← SQLite + AES storage
│   │   └── InMemoryEntryRepository.java ← Fallback (no driver needed)
│   └── ui/
│       └── ConsoleUI.java            ← Full terminal UI
├── lib/
│   └── sqlite-jdbc.jar               ← (auto-downloaded by run.sh)
├── .vscode/
│   ├── settings.json
│   ├── tasks.json
│   └── launch.json
├── run.sh                            ← Linux/macOS build+run
├── run.bat                           ← Windows build+run
└── README.md
```

---

## ⚡ Quick Start

### Prerequisites

- **Java JDK 17 or higher** — [Download from Adoptium](https://adoptium.net/)
- **VS Code** with the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

### Option A: Run Script (Recommended)

**Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

**Windows:**
```
run.bat
```

The script automatically:
1. Detects your JDK
2. Downloads the SQLite JDBC driver from Maven Central
3. Compiles all 39 source files
4. Packages into `MindCheck.jar`
5. Launches the app

### Option B: VS Code Tasks

1. Open the `MindCheck/` folder in VS Code
2. Press `Ctrl+Shift+P` → **Tasks: Run Task** → **MindCheck: Build JAR & Run**

Or use the keyboard shortcut `Ctrl+Shift+B` to build.

### Option C: Manual Compile + Run

```bash
# Create output directory
mkdir -p out

# Download SQLite JDBC (optional, skip for in-memory mode)
wget -O lib/sqlite-jdbc.jar \
  https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar

# Compile (Linux/macOS)
find src -name "*.java" > sources.txt
javac -source 17 -target 17 -cp "out:lib/sqlite-jdbc.jar" -d out @sources.txt

# Run
java -cp "out:lib/sqlite-jdbc.jar" mindcheck.Main

# Windows uses semicolons:
# javac -cp "out;lib\sqlite-jdbc.jar" -d out @sources.txt
# java -cp "out;lib\sqlite-jdbc.jar" mindcheck.Main
```

---

## 🖥️ Application Menu

```
╔══════════════════════════════════════════╗
║       🧠 MindCheck — Smart Journal        ║
╠══════════════════════════════════════════╣
║  1. ✍️   New Text Entry                   ║
║  2. 🎙️   New Voice Entry (simulated)      ║
║  3. 📖  View Entry History                ║
║  4. 📊  Weekly Report                     ║
║  5. 📈  Mood Graph                        ║
║  6. 👤  My Profile & Insights             ║
║  7. 🤖  Run Demo (auto entries)           ║
║  8. 🔬  NLP Debug Mode                    ║
║  0. 👋  Exit                              ║
╚══════════════════════════════════════════╝
```

**Option 7 (Demo)** is the fastest way to see everything — it adds 10 sample
entries covering all emotions, including conflict cases like `"I'm fine!!! 😭"`.

**Option 8 (NLP Debug)** shows the full Chain of Responsibility pipeline
step-by-step for any text you type.

---

## 🧠 OOP Concepts Demonstrated

| Concept | Where |
|---|---|
| Abstract Class | `JournalEntry` |
| Inheritance | `TextEntry`, `VoiceEntry` |
| Interface | `TextProcessor`, `MoodAnalyser`, `EntryRepository`, `MoodSuggestionStrategy`, `AnalyticsObserver` |
| Polymorphism | NLP pipeline, strategy switching |
| Encapsulation | Private fields + getters everywhere |
| Generics | `EmotionTimeline<T extends JournalEntry>` |
| Collections | `HashMap`, `LinkedHashMap`, `ArrayList`, `EnumMap` |
| Factory Pattern | `EntryFactory` |
| Chain of Responsibility | NLP Pipeline (8 stages) |
| Strategy Pattern | 3 suggestion engines |
| Observer Pattern | `StreakTracker`, `AnomalyDetector` → `EmotionTimeline` |
| Singleton Pattern | `ProfileManager` |

---

## 🔥 Hybrid Emotion Formula

```
finalScore = 0.6 × naiveBayesScore
           + 0.3 × ruleBasedScore
           + 0.1 × emojiScore
```

Applied with intensity multiplier + conflict resolution for contradictions
like positive words paired with negative emojis.

---

## 🔒 Storage

- **With SQLite driver**: Entries are AES-128 encrypted and stored in `mindcheck.db`
- **Without driver**: Falls back automatically to in-memory storage (data lost on exit)

---

## 📊 Sample Output

```
╔══════════════════════════════════════════╗
║  EMOTION DETECTED: 😊 Joyful             ║
║  Confidence: 87% (High)                  ║
╠══════════════════════════════════════════╣
║  📊 Hybrid Score Breakdown:              ║
║    Naive Bayes  (60%) :  92.14%          ║
║    Rule-Based   (30%) :  80.00%          ║
║    Emoji-Score  (10%) :  75.00%          ║
╠══════════════════════════════════════════╣
║  Valence : +0.80  |  Arousal : +0.70    ║
╚══════════════════════════════════════════╝
```
