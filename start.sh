#!/bin/bash
#
# mycontracts Startup Script (Linux/macOS)
#
# Dieses Skript startet die mycontracts Anwendung mit optimalen Einstellungen
#

set -e

# Konfiguration
JAR_FILE="mycontracts-0.0.1-SNAPSHOT.jar"
JAR_PATH="backend/target/${JAR_FILE}"
PORT=8080
LOG_FILE="mycontracts.log"

# Farben für Ausgabe
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}  mycontracts - Vertrags-Cockpit${NC}"
echo -e "${GREEN}===========================================${NC}"
echo

# Prüfe Java Installation
echo -e "${YELLOW}[1/4]${NC} Prüfe Java Installation..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java ist nicht installiert!${NC}"
    echo "Bitte installiere Java 17 oder höher:"
    echo "  - Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "  - macOS: brew install openjdk@17"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
echo -e "${GREEN}✓${NC} Java gefunden: ${JAVA_VERSION}"
echo

# Prüfe JAR-Datei
echo -e "${YELLOW}[2/4]${NC} Prüfe JAR-Datei..."
if [ ! -f "$JAR_PATH" ]; then
    echo -e "${YELLOW}JAR-Datei nicht gefunden. Baue Projekt...${NC}"
    cd backend
    if ! mvn clean package -DskipTests; then
        echo -e "${RED}ERROR: Build fehlgeschlagen!${NC}"
        exit 1
    fi
    cd ..
fi

if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}ERROR: JAR-Datei nicht gefunden: ${JAR_PATH}${NC}"
    echo "Bitte baue das Projekt zuerst:"
    echo "  cd backend && mvn clean package"
    exit 1
fi

JAR_SIZE=$(du -h "$JAR_PATH" | cut -f1)
echo -e "${GREEN}✓${NC} JAR-Datei gefunden: ${JAR_PATH} (${JAR_SIZE})"
echo

# Prüfe Port
echo -e "${YELLOW}[3/4]${NC} Prüfe Port ${PORT}..."
if command -v lsof &> /dev/null; then
    if lsof -Pi :${PORT} -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${RED}WARNING: Port ${PORT} ist bereits belegt!${NC}"
        echo "Du kannst einen anderen Port nutzen:"
        echo "  PORT=9000 $0"
        exit 1
    fi
elif command -v netstat &> /dev/null; then
    if netstat -tuln 2>/dev/null | grep ":${PORT} " | grep LISTEN >/dev/null 2>&1 ; then
        echo -e "${RED}WARNING: Port ${PORT} ist bereits belegt!${NC}"
        echo "Du kannst einen anderen Port nutzen:"
        echo "  PORT=9000 $0"
        exit 1
    fi
else
    echo -e "${YELLOW}Hinweis: Port-Check übersprungen (lsof/netstat nicht verfügbar)${NC}"
fi
echo -e "${GREEN}✓${NC} Port Check abgeschlossen"
echo

# Optional: Environment-Variablen setzen (kommentiert, da Defaults gut sind)
# export FILE_STORAGE_PATH=/opt/mycontracts/data/files
# export WATCH_DIR=/opt/mycontracts/data/incoming
# export SPRING_DATASOURCE_URL=jdbc:sqlite:/opt/mycontracts/mycontracts.db

# Starte Anwendung
echo -e "${YELLOW}[4/4]${NC} Starte mycontracts..."
echo
echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}  Server startet auf Port ${PORT}${NC}"
echo -e "${GREEN}  Logs: ${LOG_FILE}${NC}"
echo
echo -e "  Health Check: ${YELLOW}http://localhost:${PORT}/api/health${NC}"
echo -e "  API Docs:     ${YELLOW}http://localhost:${PORT}/actuator${NC}"
echo
echo -e "  Zum Beenden: ${RED}Strg+C${NC}"
echo -e "${GREEN}===========================================${NC}"
echo

# Starte JAR
java -jar "$JAR_PATH" \
    --server.port=${PORT} \
    2>&1 | tee "$LOG_FILE"
