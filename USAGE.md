# mycontracts – Nutzungsanleitung

## Quick Start

### 1. Anwendung starten

```bash
# Mit Docker Compose (empfohlen)
docker-compose up

# Oder lokal:
# Terminal 1 – Backend
cd backend && mvn spring-boot:run

# Terminal 2 – Frontend
cd frontend && npm install && npm run dev
```

**URLs:**
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- API Health: `http://localhost:8080/api/health`

---

## Workflow-Beispiel: Lizenz-Vertrag verwalten

### Szenario
Ein Software-Lizenz-Vertrag muss hochgeladen, markiert und überwacht werden.

### Schritt 1: Datei hochladen

**UI:**
1. Im Upload-Bereich "Choose File" klicken
2. `License_Software_2025.pdf` auswählen
3. "Upload" klicken
4. Status: "Upload successful ✓"
5. Datei erscheint in der Liste links

**API (curl):**
```bash
curl -F "file=@License_Software_2025.pdf" \
  http://localhost:8080/api/files/upload | jq
```

**Response:**
```json
{
  "id": 42,
  "filename": "License_Software_2025.pdf",
  "size": 15342,
  "checksum": "a3f2d8c9e1b7f4c6...",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": [],
  "dueDate": null
}
```

### Schritt 2: Datei auswählen und Detail anschauen

**UI:**
1. "License_Software_2025.pdf" in der Liste klicken
2. Datei wird hervorgehoben (blauer Rahmen)
3. Rechts im Detail-Panel werden Infos angezeigt:
   - Filename, Size, MIME Type
   - Checksumme (SHA-256)
   - Aktuelle Marker (leer)
   - Due Date (leer)
   - Notiz (leer)
   - OCR Status (falls vorhanden)

### Schritt 3: Marker setzen

Das Lizenz-System ist unvollständig. Es gibt 2 Probleme:
1. Lizenz-Informationen sind **unvollständig** → `MISSING_INFO` Marker
2. OCR konnte PDF nicht einlesen → `INCOMPLETE_OCR` Marker

**UI:**
1. Im Detail-Panel unter "Marker (Multiple)" zu den Checkboxen scrollen
2. Folgende Boxen ankreuzen:
   - ☑ `MISSING_INFO`
   - ☑ `INCOMPLETE_OCR`
   - Alle anderen bleiben unchecked (☐)
3. Button "Marker speichern" klicken
4. Status: "Speichere..." → "Fertig"
5. Datei in der Liste zeigt jetzt: `[MISSING_INFO] [INCOMPLETE_OCR]`

**API (curl):**
```bash
curl -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"markers":["MISSING_INFO","INCOMPLETE_OCR"]}' \
  http://localhost:8080/api/files/42/markers | jq
```

### Schritt 4: Fälligkeitsdatum setzen

Die Lizenz muss bis zum 15. Dezember 2025 aktualisiert sein.

**UI:**
1. Unter "Due Date (Fälligkeitsdatum)" Datum eingeben:
   - Input-Feld: `2025-12-15`
2. Button "Fälligkeitsdatum speichern" klicken
3. Status: "Speichere..." → "Fertig"
4. Datei in der Liste zeigt jetzt: `📅 15 Dec 2025`

**API (curl):**
```bash
curl -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"dueDate":"2025-12-15T00:00:00Z"}' \
  http://localhost:8080/api/files/42/due-date | jq
```

### Schritt 5: Notiz hinzufügen

Wichtige Infos für den Review.

**UI:**
1. Im Textarea unter "Notiz" Text eingeben:
   ```
   Kontaktiere Vendor für Klärung der Seat-Count.
   Prüfe Rabatt-Bedingungen für 100+ Lizenzen.
   Aktualisierung erforderlich bis 15.12.2025
   ```
2. Button "Notiz speichern" klicken
3. Status: "Speichere..." → "Fertig"
4. Datei in der Liste zeigt jetzt: `📝 Note` Badge

**API (curl):**
```bash
curl -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"note":"Kontaktiere Vendor..."}' \
  http://localhost:8080/api/files/42/note | jq
```

### Schritt 6: Datei-Detail ansehen

**UI:**
1. Datei in der Liste ist noch selected
2. Alle Infos werden rechts angezeigt:
   - ✅ 2 Marker sichtbar: `[MISSING_INFO]` `[INCOMPLETE_OCR]`
   - ✅ Due Date: `📅 15 Dec 2025`
   - ✅ Notiz mit Kontext
   - ℹ️ OCR Status (wenn vorhanden)

**API (curl):**
```bash
curl http://localhost:8080/api/files/42 | jq
```

**Response:**
```json
{
  "id": 42,
  "filename": "License_Software_2025.pdf",
  "mime": "application/pdf",
  "size": 15342,
  "checksum": "a3f2d8c9e1b7f4c6...",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": ["MISSING_INFO", "INCOMPLETE_OCR"],
  "dueDate": "2025-12-15T00:00:00Z",
  "note": "Kontaktiere Vendor für Klärung der Seat-Count...",
  "ocr": null
}
```

---

## Filter & Übersicht

### "Needs Attention" Filter

Zeigt alle Dateien mit hoher Priorität:
- Marker: `URGENT`, `REVIEW`, oder `MISSING_INFO`
- ODER: Fälligkeitsdatum ist in der Vergangenheit (überdue)

**UI:**
1. Im Filter-Dropdown unter Marker:
   - [▼ Alle] → [▼ Needs Attention]
2. Liste zeigt nur kritische Dateien

**Beispiel-Liste:**
```
├─ NDA_Acme_Corp.pdf
│  [URGENT] [REVIEW] 📅 31 Dec 2024 (TOMORROW!)
│
├─ License_Software_2025.pdf  
│  [MISSING_INFO] [INCOMPLETE_OCR] 📅 15 Dec 2025 (OVERDUE!)
│
└─ Lease_Office_Space.pdf
   [URGENT]
```

### OCR Filter

Zeige nur Dateien mit bestimmtem OCR-Status:
- `MATCHED` – OCR erfolgreich
- `PENDING` – OCR läuft noch
- `FAILED` – OCR fehlgeschlagen
- `NONE` – Keine OCR vorhanden

**UI:**
1. Im Filter-Dropdown unter OCR:
   - [▼ Alle] → [▼ MATCHED]
2. Liste zeigt nur Dateien mit OCR MATCHED

---

## Marker-Übersicht

| Marker | Farbe | Einsatz | Beispiel |
|--------|-------|---------|----------|
| **URGENT** | 🔴 Rot | Sofort handeln erforderlich | NDA mit Signaturbedarf bis morgen |
| **REVIEW** | 🟡 Gelb | Review/Unterschrift erforderlich | Service-Vertrag auf Legal-Review wartend |
| **MISSING_INFO** | 🟣 Violett | Informationen unvollständig | Lizenz-Vertrag mit Seat-Count TBD |
| **INCOMPLETE_OCR** | 🩷 Rosa | OCR nicht erfolgreich | Gescannte PDF, OCR lesbar |
| **FOLLOW_UP** | 🟢 Grün | Follow-up/Nachverfolgung | Employee-Vertrag, Unterschrift erhalten |

### Multi-Marker Beispiele

**Komplexe Situation – Mehrere Tags pro Datei:**
```
License_Software_2025.pdf
[MISSING_INFO] [INCOMPLETE_OCR] 📅 15 Dec 2025

→ Problem 1: Infos unvollständig (Seat-Count, Renewal Terms)
→ Problem 2: OCR konnte nicht automatisch extrahieren
→ Deadline: 15. Dezember (OVERDUE ab jetzt!)
```

**Einfache Situation – Ein Tag:**
```
Lease_Office_Space.pdf
[URGENT]

→ Höchste Priorität, schnelle Aktion erforderlich
```

**Keine Tags – Archiviert:**
```
Old_Contract_2023.pdf
(keine Marker)

→ Gelöst/abgeschlossen, im Archiv
```

---

## OCR Integration (optional)

Falls OCR JSON verfügbar ist:

### OCR JSON hochladen

**Format:** `contracts_ocr.json` (Dateiname muss mit `_ocr.json` enden)

**Inhalt:**
```json
{
  "filename": "License_Software_2025",
  "status": "DONE",
  "extractedText": "SOFTWARE LICENSE AGREEMENT...",
  "extractedFields": {
    "vendor": "TechVendor Inc",
    "productName": "Enterprise Software",
    "licenseType": "Volume License",
    "seats": 100,
    "validFrom": "2025-01-01",
    "validUntil": "2025-12-31"
  }
}
```

**API:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d @contracts_ocr.json \
  http://localhost:8080/api/ocr/import
```

### Watcher Service

Der Watcher überwacht automatisch `WATCH_DIR` auf neue OCR JSONs und versucht, diese einem hochgeladenen File zuzuordnen:

1. Watcher sucht Datei mit Name `License_Software_2025` (ohne `_ocr.json`)
2. Wenn gefunden → OCR wird verlinkt
3. OCR Status ändert zu `MATCHED`
4. Im Detail-Panel unter "OCR Information" sichtbar

**Config (`.env`):**
```env
WATCH_DIR=/data/incoming
watcher.scan-interval-ms=5000
watcher.max-retries=5
```

---

## Häufige Aufgaben

### Ich möchte einen alten Marker entfernen

**UI:**
1. Datei auswählen
2. Im Detail-Panel bei "Marker (Multiple)" den entsprechenden Marker **abwählen** (☐)
3. "Marker speichern" klicken

**API:**
```bash
# Alle Marker außer URGENT setzen (URGENT entfernen)
curl -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"markers":["REVIEW","MISSING_INFO"]}' \
  http://localhost:8080/api/files/42/markers
```

### Ich möchte das Due Date löschen

**UI:**
1. Datei auswählen
2. Im Detail-Panel bei "Due Date (Fälligkeitsdatum)" das Datumfeld **leeren** (alles löschen)
3. "Fälligkeitsdatum speichern" klicken

**API:**
```bash
curl -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"dueDate":null}' \
  http://localhost:8080/api/files/42/due-date
```

### Ich möchte alle Dateien als JSON exportieren

**API:**
```bash
curl http://localhost:8080/api/files > contracts_export.json
```

### Ich möchte eine Datei herunterladen

**UI:**
1. Datei auswählen
2. Im Detail-Panel gibt es evtl. einen Download-Link (nicht immer sichtbar)

**API:**
```bash
curl -o License_Software_2025.pdf \
  http://localhost:8080/api/files/42/download
```

---

## Troubleshooting

### Backend läuft nicht auf Port 8080
- Prüfe ob Port schon belegt ist: `lsof -i :8080`
- Starte Backend explizit: `cd backend && mvn spring-boot:run`
- Logs ansehen: Backend-Console sollte "Started MycontractsApplication" zeigen

### Frontend läuft nicht auf Port 5173
- Prüfe ob Port schon belegt ist: `lsof -i :5173`
- Starte Frontend explizit: `cd frontend && npm run dev`
- Browser-Logs: F12 → Console → Fehler?

### Upload schlägt fehl
- Datei ist zu groß? Max 10 MB
- Datei-Format supported? (PDF, etc.)
- Backend läuft?

### Marker/Due Date speichern funktioniert nicht
- Browser-Console öffnen (F12)
- Network-Tab: Anfrage erfolgt aber fehler?
- Backend-Logs ansehen: Error?
- Backend API testen: `curl http://localhost:8080/api/health`

---

## Tipps & Best Practices

1. **Marker sparsam einsetzen** – Ein File sollte max. 2-3 Marker haben, sonst wird es unübersichtlich
2. **Due Dates realistisch setzen** – "Needs Attention" zeigt alle überdue Dateien prominent
3. **Notizen konkret schreiben** – "Signature required from CEO, sent 30.12." ist hilfreich
4. **Regelmäßig filtern** – "Needs Attention" täglich checken für Überblick
5. **OCR nicht erzwingen** – Wenn OCR fehlschlägt, `INCOMPLETE_OCR` Marker setzen und manuell bearbeiten

---

## Roadmap (Zukünftig)

- [ ] **Tasks/Reminders Tab** – Sortierte Ansicht nach Due Date (overdue = rot, today = gelb, future = blau)
- [ ] **Bulk Actions** – Mehrere Dateien gleichzeitig mit Markern/Notes versehen
- [ ] **Contract Linking** – Mehrere PDFs zu einem Vertrag gruppieren (z.B. NDA + Signatur-Scan)
- [ ] **Advanced OCR** – LLM-basierte Feldextraktion (Gemini)
- [ ] **Audit Log** – Wer hat was wann geändert?
- [ ] **Benutzer & Rollen** – Multi-User mit unterschiedlichen Rechten
