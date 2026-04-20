#!/bin/bash
# ============================================================
#  MindCheck — Compile & Run Script (Linux / macOS)
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/out"
LIB_DIR="$PROJECT_DIR/lib"
JAR_OUT="$PROJECT_DIR/MindCheck.jar"
MANIFEST="$PROJECT_DIR/MANIFEST.MF"
SQLITE_JAR="$LIB_DIR/sqlite-jdbc.jar"
MAIN_CLASS="mindcheck.Main"

echo "============================================="
echo "   MindCheck — Build & Run"
echo "============================================="

# ── 1. Check Java ────────────────────────────────
if ! command -v javac &> /dev/null; then
    echo "❌ javac not found."
    echo "   Install JDK 11+:  sudo apt install default-jdk   (Ubuntu)"
    echo "                     brew install openjdk            (macOS)"
    exit 1
fi
echo "✅ javac: $(javac -version 2>&1)"

# ── 2. Download SQLite JDBC if missing ───────────
mkdir -p "$LIB_DIR"
if [ ! -f "$SQLITE_JAR" ] || [ ! -s "$SQLITE_JAR" ]; then
    echo "⬇️  Downloading SQLite JDBC driver..."
    if command -v wget &> /dev/null; then
        wget -q -O "$SQLITE_JAR" \
            "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"
    elif command -v curl &> /dev/null; then
        curl -sL -o "$SQLITE_JAR" \
            "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"
    else
        echo "⚠️  No wget or curl found. Running with in-memory storage."
        SQLITE_JAR=""
    fi
fi

# ── 3. Compile ───────────────────────────────────
mkdir -p "$OUT_DIR"
echo "⚙️  Compiling sources..."

CLASSPATH="$OUT_DIR"
[ -f "$SQLITE_JAR" ] && [ -s "$SQLITE_JAR" ] && CLASSPATH="$CLASSPATH:$SQLITE_JAR"

find "$SRC_DIR" -name "*.java" > /tmp/mindcheck_sources.txt
javac -source 17 -target 17 \
      -encoding UTF-8 \
      -cp "$CLASSPATH" \
      -d "$OUT_DIR" \
      @/tmp/mindcheck_sources.txt

echo "✅ Compilation successful!"

# ── 4. Package JAR ───────────────────────────────
echo "📦 Packaging JAR..."
cat > "$MANIFEST" <<EOF
Manifest-Version: 1.0
Main-Class: $MAIN_CLASS
EOF

# Include SQLite inside jar if available
if [ -f "$SQLITE_JAR" ] && [ -s "$SQLITE_JAR" ]; then
    # Extract sqlite jar into out dir
    cd "$OUT_DIR" && jar xf "$SQLITE_JAR" && cd "$PROJECT_DIR"
fi

jar cfm "$JAR_OUT" "$MANIFEST" -C "$OUT_DIR" .
echo "✅ JAR created: MindCheck.jar"

# ── 5. Run ───────────────────────────────────────
echo ""
echo "🚀 Starting MindCheck..."
echo "============================================="
java -jar "$JAR_OUT"
