@echo off
REM ============================================================
REM  MindCheck — Compile & Run Script (Windows)
REM ============================================================

setlocal EnableDelayedExpansion

set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set OUT_DIR=%PROJECT_DIR%out
set LIB_DIR=%PROJECT_DIR%lib
set JAR_OUT=%PROJECT_DIR%MindCheck.jar
set MANIFEST=%PROJECT_DIR%MANIFEST.MF
set SQLITE_JAR=%LIB_DIR%\sqlite-jdbc.jar
set MAIN_CLASS=mindcheck.Main

echo =============================================
echo    MindCheck — Build ^& Run (Windows)
echo =============================================

REM ── 1. Check Java ──────────────────────────────
where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] javac not found.
    echo   Download JDK 17+ from: https://adoptium.net/
    echo   Then add it to your PATH.
    pause
    exit /b 1
)

for /f "delims=" %%i in ('javac -version 2^>^&1') do echo [OK] %%i

REM ── 2. Download SQLite JDBC ────────────────────
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

set SQLITE_URL=https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar
if not exist "%SQLITE_JAR%" (
    echo [INFO] Downloading SQLite JDBC driver...
    powershell -Command "Invoke-WebRequest -Uri '%SQLITE_URL%' -OutFile '%SQLITE_JAR%'" 2>nul
    if not exist "%SQLITE_JAR%" (
        echo [WARN] Download failed. Running with in-memory storage.
        set SQLITE_JAR=
    )
)

REM ── 3. Compile ─────────────────────────────────
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
echo [....] Compiling sources...

REM Collect all .java files
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\mindcheck_sources.txt"

set CP=%OUT_DIR%
if defined SQLITE_JAR if exist "%SQLITE_JAR%" set CP=%CP%;%SQLITE_JAR%

javac -source 17 -target 17 -encoding UTF-8 -cp "%CP%" -d "%OUT_DIR%" @"%TEMP%\mindcheck_sources.txt"
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [OK] Compilation successful!

REM ── 4. Package JAR ─────────────────────────────
echo [....] Packaging JAR...
(
    echo Manifest-Version: 1.0
    echo Main-Class: %MAIN_CLASS%
) > "%MANIFEST%"

REM Extract sqlite into out if available
if defined SQLITE_JAR if exist "%SQLITE_JAR%" (
    pushd "%OUT_DIR%"
    jar xf "%SQLITE_JAR%"
    popd
)

jar cfm "%JAR_OUT%" "%MANIFEST%" -C "%OUT_DIR%" .
echo [OK] JAR created: MindCheck.jar

REM ── 5. Run ─────────────────────────────────────
echo.
echo [RUN] Starting MindCheck...
echo =============================================
java -jar "%JAR_OUT%"

pause
