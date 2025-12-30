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
- `LLM_PROVIDER` / `GEMINI_API_KEY` – LLM Konfiguration (optional).
- `SPRING_DATASOURCE_URL` – SQLite DataSource URL (Default: `jdbc:sqlite:mycontracts.db`).

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