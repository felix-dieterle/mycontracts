# Frontend Layout Plan

## Ziele
- Schnelle Orientierung: Liste links, Detail rechts, Upload jederzeit erreichbar.
- Deep-Linking: Detailansicht per URL (z. B. `/files/:id`).
- Minimaler State: Server ist Single Source of Truth; Client cached nur für Anzeige.

## Routen
- `/` – Default-Split-View (Liste + Detail). Leitet auf das erste verfügbare File um (`/files/:id`), falls noch keine ID gewählt.
- `/files/:id` – Detailansicht für eine Datei mit gleicher Split-View; Auswahl in der Liste folgt der URL.
- `/upload` (optional) – Fokus auf Upload-Form; nach erfolgreichem Upload Redirect nach `/files/:id` der neu erstellten Datei.

## Layout-Abschnitte
- **Header**: App-Name, Backend-Status-Badge, ggf. Link zu Metrics/Actuator, Upload-Button.
- **Main Grid**: Zwei Spalten
  - **Liste (links, 320px)**: Dateiname, Größe, Datum, OCR-Status-Badge (pending/matched/failed). Klick setzt `:id` in der URL.
  - **Detail (rechts)**: Metadaten (Name, Größe, MIME, Checksumme), Download-Link, OCR-Panel (Status, Retry-Count, Raw JSON prettified), Platzhalter für Extraktionsfelder (coming soon).
- **Upload Panel**: Eingebettet im Header oder als kurzer Abschnitt oberhalb des Grids.

## API-Verbrauch
- `GET /api/files` → Liste, liefert Metadaten.
- `GET /api/files/{id}` → Detail inkl. OCR.
- `POST /api/files/upload` → Upload; Rückgabe enthält `id` für Redirect.
- `GET /api/files/{id}/download` → Download-Link.

## State & Routing
- Router (React Router) steuert `selectedId` aus `:id` Param.
- Beim Laden der Liste: wenn `:id` fehlt, nimm erstes Element und navigiere zu `/files/:id`.
- Bei Upload: nach Erfolg navigiere zu `/files/{created.id}` und refreshe Liste/Detail.

## UX-Notizen
- Lade-/Fehlerbadges in Liste und Detail klar sichtbar.
- OCR-Status-Badges farblich differenziert (pending gelb, matched grün, failed rot).
- Mobile: Stack statt Grid (Liste über Detail), Routing bleibt gleich.

## Open Punkte
- Extraktionsfelder: Bereich im Detail reservieren.
- Auth: Falls Header-Token kommt, globale Fetch-Helper vorbereiten.
