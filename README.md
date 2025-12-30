# mycontracts

Leichtgewichtige Vertragsverwaltung (MVP) mit OCR-Matching, Status-Markern und Task-Management.

## Features

✅ **Datei-Upload** mit automatischer Checksumme und Metadaten  
✅ **OCR-Watcher** – Automatisches Matching von OCR-JSONs zu Dateien mit Retry-Logik  
✅ **Multi-Marker-System** – Mehrere unabhängige Tags pro Vertrag (URGENT, REVIEW, etc.)  
✅ **Due Dates** – Fälligkeitsdaten für Task-like Workflow  
✅ **Notizen** – Kurze Vermerke zu Reviews, Risiken, TODOs  
✅ **Smart Filtering** – "Needs Attention" findet dringende/überfällige Verträge  
✅ **Rest API** – Volle CRUD-Operationen auf Dateien und Metadaten  
✅ **Micrometer Metrics** – Prometheus-kompatible Health & Metrics Endpoints  

**Weitere Dokumentation:**
- [USAGE.md](USAGE.md) – Schritt-für-Schritt Nutzung mit UI- und API-Beispielen
- [API.md](API.md) – Vollständige REST API Referenz (Marker, Due Dates, Notes)

## Benutzeroberfläche

### Übersicht – Dateiliste und Filter

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  mycontracts                                    🏥 Health: OK               │
├────────────────────────────────────┬────────────────────────────────────────┤
│                                    │                                        │
│  Upload File    Upload Verzeichnis │  📄 Dateien         (Lädt... / Fehler)│
│  [Choose File] [Upload]            │                                        │
│  Status: idle / Upload successful  │  Filter nach Marker:                   │
│                                    │  [▼ Alle / Needs Attention]            │
│  HRESULT: OK (status: UP)          │                                        │
│                                    │  Filter nach OCR:                      │
│                                    │  [▼ Alle / MATCHED / PENDING / FAILED]│
│                                    │                                        │
│                                    │  Legende:                              │
│                                    │  [URGENT] [REVIEW] [MISSING_INFO]      │
│                                    │  [INCOMPLETE_OCR] [FOLLOW_UP]          │
│                                    │  [OCR MATCHED] [OCR PENDING]           │
│                                    │  [OCR FAILED]                          │
│                                    │                                        │
└────────────────────────────────────┴────────────────────────────────────────┘
```

### Dateiliste mit Markern und Due Dates

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  📄 Dateien                                                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ├─ NDA_Acme_Corp.pdf                                                      │
│  │  4.2 KB · 30 Dec 2024      [URGENT] [REVIEW] 📅 31 Dec 2024             │
│  │                            OCR MATCHED       📝 Note                    │
│  │                                                                         │
│  ├─ Service_Agreement_TechVendor.pdf                                       │
│  │  3.8 KB · 30 Dec 2024      [REVIEW] 📅 2 Jan 2026                       │
│  │                            OCR PENDING                                  │
│  │                                                                         │
│  ├─ License_Software_2025.pdf (SELECTED)                                   │
│  │  2.1 KB · 30 Dec 2024      [MISSING_INFO] [INCOMPLETE_OCR]             │
│  │                            📅 15 Dec 2025 (OVERDUE) OCR FAILED          │
│  │                                                                         │
│  ├─ Employee_Contract_Jane_Doe.pdf                                         │
│  │  5.3 KB · 30 Dec 2024      [FOLLOW_UP] 📅 15 Feb 2026                  │
│  │                            OCR MATCHED                                  │
│  │                                                                         │
│  └─ Lease_Office_Space.pdf                                                 │
│     2.9 KB · 30 Dec 2024      [URGENT]                                     │
│                               OCR MATCHED       📝 Note                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Detail-Panel mit Marker-Checkboxen und Due Date

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  📋 Detail                                                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Datei                                                                      │
│  License_Software_2025.pdf                                                 │
│  2.1 KB · application/pdf · 30 Dec 2024                                    │
│                                                                             │
│  Checksumme                                                                 │
│  a3f2d8c9e1b7f4c6... (truncated)                                           │
│                                                                             │
│  Marker (Multiple)                                                          │
│  ☑ [URGENT]           ☐ [REVIEW]           ☐ [MISSING_INFO]              │
│  ☑ [INCOMPLETE_OCR]   ☐ [FOLLOW_UP]                                        │
│  [Marker speichern] (Saving... / Saved)                                    │
│                                                                             │
│  Due Date (Fälligkeitsdatum)                                               │
│  [2025-12-15]                                                               │
│  [Due Date speichern] (Saving... / Saved)                                  │
│                                                                             │
│  Notiz                                                                      │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │ Kontaktiere Vendor für fehlende Informationen zur Lizenz.         │   │
│  │ Aktualisierung erforderlich bis 15.12.                            │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│  [Notiz speichern]                                                         │
│                                                                             │
│  OCR Information                                                            │
│  Status: FAILED                                                             │
│  Retry Count: 3 / 5                                                         │
│  Last Attempt: 30 Dec 2024, 13:45                                          │
│  Raw JSON (truncated):                                                     │
│  {                                                                          │
│    "status": "FAILED",                                                      │
│    "error": "Unreadable PDF",                                              │
│    "timestamp": "2024-12-30T13:45:00Z"                                     │
│  }                                                                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Marker-System

Das System unterstützt **5 unabhängige Marker** pro Vertrag:

| Marker | Farbe | Einsatz |
|--------|-------|---------|
| **URGENT** | 🔴 Rot | Höchste Priorität, sofort handeln |
| **REVIEW** | 🟡 Gelb | Benötigt Review/Unterschrift |
| **MISSING_INFO** | 🟣 Violett | Informationen unvollständig |
| **INCOMPLETE_OCR** | 🩷 Rosa | OCR nicht erfolgreich |
| **FOLLOW_UP** | 🟢 Grün | Follow-up / Nachverfolgung erforderlich |

**Beispiel:** Ein Lizenz-Vertrag kann gleichzeitig `[MISSING_INFO]` + `[INCOMPLETE_OCR]` haben (komplexe Situation wird erfasst).

## Quickstart

1. Kopiere `.env.example` nach `.env` und passe ggf. Pfade an (z. B. `WATCH_DIR` und `FILE_STORAGE_PATH`).
2. Starte das Projekt lokal:

```bash
docker-compose up
```

Backend Health: `http://localhost:8080/api/health`
Frontend: `http://localhost:5173`

## Konfiguration

Wichtige Umgebungsvariablen (in `.env`):
- `FILE_STORAGE_PATH` – Verzeichnis zum Speichern hochgeladener Dateien (Default: `/data/files`).
- `WATCH_DIR` – beobachtetes Verzeichnis für OCR JSONs (Default: `/data/incoming`).
- `watcher.scan-interval-ms` – Scanintervall in Millisekunden (Default: `5000`).
- `watcher.max-retries` – Anzahl der Wiederholungsversuche für nicht zugeordnete OCRs (Default: `5`).
- `LLM_PROVIDER` / `GEMINI_API_KEY` – LLM Konfiguration (optional).
- `SPRING_DATASOURCE_URL` – SQLite DataSource URL (Default: `jdbc:sqlite:mycontracts.db`).

### Watcher Service

Der Watcher überwacht das Verzeichnis `WATCH_DIR` nach OCR JSON Dateien mit dem Suffix `_ocr.json` und versucht, diese automatisch einem bereits hochgeladenen `StoredFile` zuzuordnen.


Wichtiges Verhalten:
- Matching-Strategie: Ein OCR mit Namen `vertragxy_ocr.json` wird gegen vorhandene Dateien verglichen, indem die Basis des Dateinamens (`vertragxy`) mit dem Basename (ohne Extension) der `StoredFile.filename` verglichen wird (z. B. `vertragxy.pdf` matcht `vertragxy_ocr.json`).
- Statuswerte in `OcrFile`: `PENDING` (noch nicht zugeordnet), `MATCHED` (erfolgreich zugeordnet), `FAILED` (nach zu vielen Versuchen nicht zugeordnet), `PROCESSING`, `DONE`.
- Wiederholungen: Der Watcher versucht periodisch, `PENDING` OCRs neu zu matchen. Die Anzahl der Versuche steuerst du mit `watcher.max-retries`.
- Fehler/Permissions: Wenn das Watch-Verzeichnis nicht zugreifbar ist (z. B. mangelnde Berechtigungen), deaktiviert sich der Watcher automatisch beim Start und loggt eine Warnung, statt die Anwendung abbrechen zu lassen.

Operative Hinweise:
- `WATCH_DIR` kann ein gemountetes Verzeichnis sein; achte auf Berechtigungen des Nutzerkontos, das den Backend-Prozess ausführt.
- Testumgebungen verwenden standardmäßig ein temporäres Watch- und Storage-Verzeichnis via `DynamicPropertySource` in Tests.

### Observability

- Actuator Health: `GET /actuator/health` (Basis-Check)
- Micrometer Metrics: `GET /actuator/metrics` (Übersicht) und `GET /actuator/metrics/{name}` für Details
- Prometheus Scrape: `GET /actuator/prometheus` (falls `management.endpoints.web.exposure.include` auf `prometheus` steht; im Default aktiv)
- Wichtige Counter:
	- `watcher.ocr.matched` – zugeordnete OCRs
	- `watcher.ocr.pending` – aktuell noch nicht zugeordnete OCRs
	- `watcher.ocr.failed` – endgültig fehlgeschlagene Zuordnungen nach Max-Retries
	- `watcher.ocr.retry` – erneute Zuordnungsversuche (inkl. Backoff)
  
Hinweise: Actuator und Micrometer sind im Backend aktiviert; kein zusätzlicher Code notwendig. Für produktive Scrapes kannst du `management.endpoints.web.base-path` oder Credentials in `application.yml`/`.env` anpassen.



## REST API

### Datei-Verwaltung

#### List Files
```http
GET /api/files
```

**Response:**
```json
[
  {
    "id": 1,
    "filename": "NDA_Acme_Corp.pdf",
    "mime": "application/pdf",
    "size": 4200,
    "checksum": "a3f2d8c9...",
    "createdAt": "2024-12-30T15:30:00Z",
    "markers": ["URGENT", "REVIEW"],
    "dueDate": "2024-12-31T00:00:00Z",
    "ocrStatus": "MATCHED"
  }
]
```

#### Get File Detail
```http
GET /api/files/{id}
```

**Response:**
```json
{
  "id": 1,
  "filename": "NDA_Acme_Corp.pdf",
  "mime": "application/pdf",
  "size": 4200,
  "checksum": "a3f2d8c9...",
  "createdAt": "2024-12-30T15:30:00Z",
  "markers": ["URGENT", "REVIEW"],
  "dueDate": "2024-12-31T00:00:00Z",
  "note": "Signature required from legal team",
  "ocr": {
    "id": 5,
    "status": "MATCHED",
    "createdAt": "2024-12-30T15:35:00Z",
    "processedAt": "2024-12-30T15:40:00Z",
    "retryCount": 0,
    "rawJson": "{...}"
  }
}
```

#### Upload File
```http
POST /api/files/upload
Content-Type: multipart/form-data

file: <binary>
```

**Response:**
```json
{
  "id": 42,
  "filename": "Contract_2025.pdf",
  "mime": "application/pdf",
  "size": 15342,
  "checksum": "f7e1d3c9...",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": [],
  "dueDate": null
}
```

#### Download File
```http
GET /api/files/{id}/download
```

### Marker-Management

#### Update Markers
```http
PATCH /api/files/{id}/markers
Content-Type: application/json

{
  "markers": ["URGENT", "REVIEW"]
}
```

**Response:** Updated `StoredFile` object

#### Update Due Date
```http
PATCH /api/files/{id}/due-date
Content-Type: application/json

{
  "dueDate": "2024-12-31T00:00:00Z"
}
```

Zum Löschen: `dueDate: null`

### Notiz-Management

#### Update Note
```http
PATCH /api/files/{id}/note
Content-Type: application/json

{
  "note": "Signature required, legal team contacted"
}
```

### Health & Monitoring

#### Health Check
```http
GET /api/health
```

**Response:**
```json
{
  "status": "UP"
}
```

#### Metrics (Prometheus)
```http
GET /api/actuator/metrics
GET /api/actuator/prometheus
```

## Architekturkurzfassung

- **Backend:** Spring Boot 3.2 (Java 17), REST API, JPA/Hibernate, SQLite Runtime
- **Frontend:** React 18 + React Router v6, Vite, TypeScript
- **OCR Watcher:** Beobachtet `WATCH_DIR` für OCR JSONs (`*_ocr.json`), matcht automatisch mit hochgeladenen Dateien
- **Metrics:** Micrometer, Prometheus-kompatibel, `/actuator/health` & `/actuator/prometheus`
- **Database:** SQLite (Runtime), H2 (Tests)
- **Docker:** docker-compose.yml für lokale Entwicklung

## Projektstruktur

```
mycontracts/
├── backend/                          # Spring Boot REST API
│   ├── src/main/java/de/flexis/...
│   │   ├── controller/               # REST endpoints
│   │   ├── service/                  # Business logic & Watcher
│   │   ├── model/                    # JPA entities (StoredFile, OcrFile, Contract)
│   │   ├── repository/               # Spring Data JPA repositories
│   │   └── MycontractsApplication.java
│   └── pom.xml
├── frontend/                         # React + Vite
│   ├── src/App.tsx                   # Main component with list/detail split view
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
├── docker-compose.yml                # Local dev setup
├── .env.example                      # Environment variables template
└── README.md
```

## Entwicklung lokal

### Prerequisites
- Java 17+
- Node.js 18+
- npm/yarn

### Backend starten
```bash
cd backend
mvn spring-boot:run
```

Backend läuft auf `http://localhost:8080`

### Frontend starten (hot reload)
```bash
cd frontend
npm install
npm run dev
```

Frontend läuft auf `http://localhost:5173`

### Tests

#### Backend Tests
```bash
cd backend
mvn test
```

#### Frontend UI Tests
Siehe [TESTING.md](TESTING.md) für automatisierte UI-Tests mit Playwright:

```bash
cd frontend

# Tests ausführen (Screenshots werden aufgenommen)
npm run test:ui

# Tests mit Browserfenster anschauen
npm run test:ui:headed

# Testbericht öffnen
npx playwright show-report
```

Die UI-Tests:
- ✅ Navigieren durch die gesamte App
- ✅ Machen Screenshots bei jedem Schritt
- ✅ Können als visuelle Dokumentation verwendet werden
- ✅ Generieren HTML-Reports mit Trace Logs

## Nächste Schritte / Roadmap

- [ ] Tasks/Reminders Tab – Sortiert nach Due Date
- [ ] Bulk Actions – Mehrere Dateien gleichzeitig bearbeiten
- [ ] Contract Linking – Mehrere Dateien zu einem Vertrag
- [ ] Advanced OCR – LLM-basierte Feldextraktion
- [ ] Audit Log – Änderungshistorie
- [ ] Benutzer & Rollen (optional)
