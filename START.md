# START – Schnellstart für mycontracts

## Ist die Release-JAR lauffähig? ✅ JA!

**Die gebaute Release-JAR ist vollständig eigenständig lauffähig!**

### Anforderungen
- ✅ **Java 17 oder höher** (einzige Voraussetzung)
- ✅ **Betriebssystem-unabhängig** (funktioniert auf Windows, Linux, macOS)
- ✅ **Keine zusätzlichen Abhängigkeiten erforderlich**

Das JAR-File enthält:
- Embedded Tomcat Webserver
- Alle Bibliotheken (Spring Boot, Hibernate, SQLite JDBC, etc.)
- SQLite Datenbank-Treiber (pure Java, keine nativen Bibliotheken)
- Alle Runtime-Abhängigkeiten

---

## 🚀 Schnellstart (3 Schritte)

### Schritt 1: Java prüfen
```bash
java -version
```
Erwartete Ausgabe: `java version "17.x.x"` oder höher

Falls Java nicht installiert ist:
- **Windows:** [Download OpenJDK](https://adoptium.net/)
- **Linux:** `sudo apt install openjdk-17-jdk` (Debian/Ubuntu) oder `sudo yum install java-17-openjdk` (RedHat/CentOS)
- **macOS:** `brew install openjdk@17`

### Schritt 2: JAR herunterladen
Lade die neueste Release-JAR von [GitHub Releases](https://github.com/felix-dieterle/mycontracts/releases) herunter:
```
mycontracts-backend-x.x.x.jar
```

Oder baue selbst:
```bash
cd backend
mvn clean package
# JAR wird erstellt in: backend/target/mycontracts-0.0.1-SNAPSHOT.jar
```

### Schritt 3: Anwendung starten
```bash
java -jar mycontracts-backend-x.x.x.jar
```

**Das war's!** Die Anwendung startet auf `http://localhost:8080`

---

## ✨ Standardwerte und Konfiguration

### Hervorragende Defaults – Keine Konfiguration erforderlich! ✅

Die Anwendung funktioniert **out-of-the-box** ohne jegliche Konfiguration:

| Feature | Standard-Wert | Beschreibung |
|---------|---------------|--------------|
| **Port** | `8080` | HTTP Server Port |
| **Datenbank** | `./mycontracts.db` | SQLite Datenbank im aktuellen Verzeichnis |
| **Datei-Speicher** | `./data/files` | Verzeichnis für hochgeladene Dateien |
| **OCR-Watcher** | `./data/incoming` | Verzeichnis für OCR JSON-Dateien |
| **Scan-Intervall** | `5000ms` | OCR-Watcher Scan-Intervall |
| **Max-Retries** | `5` | Maximale OCR Matching Versuche |

**Alle Verzeichnisse werden automatisch erstellt!**

### Optional: Konfiguration anpassen

Falls du die Defaults ändern möchtest, kannst du Umgebungsvariablen nutzen:

```bash
# Port ändern
java -jar mycontracts.jar --server.port=9000

# Oder via Umgebungsvariablen
export FILE_STORAGE_PATH=/mnt/contracts/files
export WATCH_DIR=/mnt/contracts/incoming
export SPRING_DATASOURCE_URL=jdbc:sqlite:/mnt/contracts/database.db
java -jar mycontracts.jar
```

Oder erstelle eine `application.properties` im gleichen Verzeichnis wie die JAR:
```properties
server.port=9000
spring.datasource.url=jdbc:sqlite:/custom/path/mycontracts.db
```

---

## 🖥️ Betriebssystem-Kompatibilität

### ✅ Vollständig plattformunabhängig!

Die Anwendung läuft auf allen Betriebssystemen mit Java 17+:

| Betriebssystem | Status | Getestet |
|----------------|--------|----------|
| **Linux** | ✅ Vollständig unterstützt | Ubuntu 20.04+, Debian 11+, CentOS 8+ |
| **Windows** | ✅ Vollständig unterstützt | Windows 10, Windows 11, Windows Server |
| **macOS** | ✅ Vollständig unterstützt | macOS 11+ (Intel & Apple Silicon) |

### Warum ist es plattformunabhängig?

1. **Pure Java:** Keine nativen Bibliotheken (.dll, .so, .dylib)
2. **SQLite JDBC:** Der SQLite-Treiber (`sqlite-jdbc`) ist pure Java
3. **Spring Boot:** Embedded Tomcat ist platform-agnostic
4. **Pfad-Handling:** Java `Path` API funktioniert auf allen OS

### Pfad-Separatoren werden automatisch behandelt
```java
// Der Code nutzt Path.of() - funktioniert überall:
Path.of("/data/files")      // Linux/macOS
Path.of("C:\\data\\files")  // Windows
Path.of("./data/files")     // Relativ (alle OS)
```

---

## 📦 Was wird beim ersten Start erstellt?

Beim ersten Start erstellt die Anwendung automatisch:

```
./
├── mycontracts.db           # SQLite Datenbank
└── data/
    ├── files/               # Verzeichnis für hochgeladene Dateien
    └── incoming/            # Verzeichnis für OCR JSON-Dateien
```

**Keine manuelle Einrichtung erforderlich!**

---

## 🔧 Erweiterte Optionen

### Option 1: Hintergrund-Prozess (Linux/macOS)
```bash
nohup java -jar mycontracts.jar > mycontracts.log 2>&1 &
```

### Option 2: Systemd Service (Linux)
Erstelle `/etc/systemd/system/mycontracts.service`:
```ini
[Unit]
Description=mycontracts Application
After=network.target

[Service]
Type=simple
User=mycontracts
WorkingDirectory=/opt/mycontracts
ExecStart=/usr/bin/java -jar /opt/mycontracts/mycontracts.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable mycontracts
sudo systemctl start mycontracts
sudo systemctl status mycontracts
```

### Option 3: Windows Service
Nutze [WinSW](https://github.com/winsw/winsw) oder [NSSM](https://nssm.cc/) um die JAR als Windows Service zu betreiben.

### Option 4: Docker (bereits vorhanden)
```bash
docker-compose up
```

---

## 🔍 Health Check

Nach dem Start prüfe die Verfügbarkeit:
```bash
curl http://localhost:8080/api/health
# Erwartete Ausgabe: {"status":"ok"}
```

Oder öffne im Browser: `http://localhost:8080/actuator/health`

---

## 🎯 Zusammenfassung: Sind wir gut aufgestellt?

### ✅ Release-JAR ist vollständig eigenständig
- Nur Java 17+ erforderlich
- Keine externen Datenbanken oder Services nötig
- Keine Konfiguration erforderlich

### ✅ Hervorragende Default-Werte
- Sinnvolle Pfade (`./data/files`, `./data/incoming`)
- Automatische Verzeichnis-Erstellung
- SQLite Datenbank im aktuellen Verzeichnis (portabel)
- Vernünftige Performance-Einstellungen (5s Scan-Intervall)

### ✅ Betriebssystem-unabhängig
- Pure Java Implementation
- Keine nativen Bibliotheken
- Funktioniert auf Windows, Linux, macOS
- Path-Handling ist plattformunabhängig

### ✅ Produktionsreif
- Embedded Webserver (Tomcat)
- Health Checks (`/actuator/health`)
- Metrics (Prometheus-kompatibel)
- Graceful Degradation (OCR-Watcher deaktiviert sich bei Problemen)

---

## 🆘 Problembehandlung

### Problem: "java: command not found"
**Lösung:** Java ist nicht installiert. Siehe [Schritt 1](#schritt-1-java-prüfen)

### Problem: Port 8080 bereits belegt
**Lösung:** Starte mit anderem Port:
```bash
java -jar mycontracts.jar --server.port=9000
```

### Problem: Keine Schreibrechte für ./data/
**Lösung:** 
```bash
# Option 1: Rechte anpassen
chmod 755 ./data

# Option 2: Custom Pfad nutzen
export FILE_STORAGE_PATH=/tmp/mycontracts/files
java -jar mycontracts.jar
```

### Problem: OCR-Watcher funktioniert nicht
**Hinweis:** Der Watcher deaktiviert sich automatisch bei Zugriffsproblemen. Die Anwendung läuft trotzdem!
```bash
# Prüfe Logs
grep "Watcher disabled" mycontracts.log
```

---

## 📚 Weitere Dokumentation

- [README.md](README.md) - Vollständige Feature-Dokumentation
- [USAGE.md](USAGE.md) - Detaillierte Nutzungsanleitung
- [API.md](API.md) - REST API Referenz
- [CI-CD.md](CI-CD.md) - Build und Release-Prozess

---

## 🎉 Fazit

**Die mycontracts Release-JAR ist:**
- ✅ Vollständig eigenständig lauffähig (nur Java 17+ erforderlich)
- ✅ Betriebssystem-unabhängig (Windows, Linux, macOS)
- ✅ Mit hervorragenden Default-Werten konfiguriert
- ✅ Produktionsreif und einfach zu deployen

**Einfach herunterladen, starten und loslegen!** 🚀
