@echo off
REM mycontracts Startup Script (Windows)
REM
REM Dieses Skript startet die mycontracts Anwendung mit optimalen Einstellungen
REM

setlocal

REM Konfiguration
set JAR_FILE=mycontracts-0.0.1-SNAPSHOT.jar
set JAR_PATH=backend\target\%JAR_FILE%
set PORT=8080
set LOG_FILE=mycontracts.log

echo ==========================================
echo   mycontracts - Vertrags-Cockpit
echo ==========================================
echo.

REM Pruefe Java Installation
echo [1/4] Pruefe Java Installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java ist nicht installiert!
    echo Bitte installiere Java 17 oder hoeher von:
    echo   https://adoptium.net/
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo [OK] Java gefunden: %JAVA_VERSION%
echo.

REM Pruefe JAR-Datei
echo [2/4] Pruefe JAR-Datei...
if not exist "%JAR_PATH%" (
    echo JAR-Datei nicht gefunden. Versuche Build...
    cd backend
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ERROR: Build fehlgeschlagen!
        cd ..
        pause
        exit /b 1
    )
    cd ..
)

if not exist "%JAR_PATH%" (
    echo ERROR: JAR-Datei nicht gefunden: %JAR_PATH%
    echo Bitte baue das Projekt zuerst:
    echo   cd backend
    echo   mvn clean package
    pause
    exit /b 1
)

for %%A in ("%JAR_PATH%") do set JAR_SIZE=%%~zA
echo [OK] JAR-Datei gefunden: %JAR_PATH% (%JAR_SIZE% Bytes)
echo.

REM Pruefe Port (optional - Windows netstat)
echo [3/4] Pruefe Port %PORT%...
netstat -ano | findstr ":%PORT%" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo WARNING: Port %PORT% ist moeglicherweise bereits belegt!
    echo Du kannst einen anderen Port nutzen:
    echo   set PORT=9000
    echo   start.bat
    pause
)
echo [OK] Port Check abgeschlossen
echo.

REM Optional: Environment-Variablen setzen (auskommentiert, da Defaults gut sind)
REM set FILE_STORAGE_PATH=C:\mycontracts\data\files
REM set WATCH_DIR=C:\mycontracts\data\incoming
REM set SPRING_DATASOURCE_URL=jdbc:sqlite:C:\mycontracts\mycontracts.db

REM Starte Anwendung
echo [4/4] Starte mycontracts...
echo.
echo ==========================================
echo   Server startet auf Port %PORT%
echo   Logs: %LOG_FILE%
echo.
echo   Health Check: http://localhost:%PORT%/api/health
echo   API Docs:     http://localhost:%PORT%/actuator
echo.
echo   Zum Beenden: Strg+C
echo ==========================================
echo.

REM Starte JAR
java -jar "%JAR_PATH%" --server.port=%PORT% > %LOG_FILE% 2>&1

endlocal
