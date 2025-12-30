# mycontracts

Leichtgewichtige Vertragsverwaltung (MVP)

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



## Wichtige Endpunkte (MVP)

- `GET /api/health` – Health Check
- `POST /api/files/upload` – Datei Upload (multipart/form-data, Feld `file`)
- `GET /api/files/{id}/download` – Datei herunterladen
- `POST /api/ocr/import` – OCR JSON importieren (body: JSON)
- `GET /api/ocr/pending` – OCRs in pending status

## Architekturkurzfassung

- Backend: Spring Boot (REST), JPA (SQLite)
- Frontend: React + Vite
- Docker Compose für lokale Entwicklung
- Watcher Service beobachtet `WATCH_DIR` und matched OCR JSONs mit Contracts (`basename_ocr.json`)

## Nächste Schritte

- Upload API, Watcher, Extraction Rules
- UI: Upload, List, Contract Detail
- LLM Adapter skeleton (Gemini default)

## Projektstruktur

- `backend/` – Spring Boot
- `frontend/` – React + Vite
- `docker-compose.yml`
- `.env.example`

---

Bei Fragen oder Wünschen zur Anpassung sag Bescheid — ich setze jetzt die Upload API & FileStorage um.