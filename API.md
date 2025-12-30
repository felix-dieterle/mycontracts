# mycontracts – REST API Reference

## Base URL

```
http://localhost:8080/api
```

## Authentication

Currently **no authentication** required (MVP stage).

---

## Files API

### List all files

```http
GET /api/files
```

**Parameters:** None

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "filename": "NDA_Acme_Corp.pdf",
    "mime": "application/pdf",
    "size": 4200,
    "checksum": "a3f2d8c9e1b7f4c6a2d5e8f1b4c7d0e3",
    "createdAt": "2024-12-30T15:30:00Z",
    "markers": ["URGENT", "REVIEW"],
    "dueDate": "2024-12-31T00:00:00Z",
    "ocrStatus": "MATCHED"
  },
  {
    "id": 2,
    "filename": "Service_Agreement.pdf",
    "mime": "application/pdf",
    "size": 3800,
    "checksum": "b4g3e9h0f2i5j8k1l4m7n0p3q6r9s2t5",
    "createdAt": "2024-12-30T15:35:00Z",
    "markers": ["REVIEW"],
    "dueDate": "2026-01-02T00:00:00Z",
    "ocrStatus": "PENDING"
  }
]
```

---

### Get file detail

```http
GET /api/files/{id}
```

**Path Parameters:**
- `{id}` (number, required) – File ID

**Response (200 OK):**
```json
{
  "id": 1,
  "filename": "NDA_Acme_Corp.pdf",
  "mime": "application/pdf",
  "size": 4200,
  "checksum": "a3f2d8c9e1b7f4c6a2d5e8f1b4c7d0e3",
  "createdAt": "2024-12-30T15:30:00Z",
  "markers": ["URGENT", "REVIEW"],
  "dueDate": "2024-12-31T00:00:00Z",
  "note": "Signature required from legal team before 31 Dec",
  "ocr": {
    "id": 5,
    "status": "MATCHED",
    "createdAt": "2024-12-30T15:35:00Z",
    "processedAt": "2024-12-30T15:40:00Z",
    "retryCount": 0,
    "rawJson": "{\"extractedText\": \"NON-DISCLOSURE AGREEMENT...\", \"confidence\": 0.92}"
  }
}
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2024-12-30T15:50:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "File not found"
}
```

---

### Upload file

```http
POST /api/files/upload
Content-Type: multipart/form-data
```

**Request Body:**
```
file: <binary file content>
```

**Example (curl):**
```bash
curl -F "file=@/path/to/NDA_Acme_Corp.pdf" \
  http://localhost:8080/api/files/upload
```

**Response (200 OK):**
```json
{
  "id": 42,
  "filename": "NDA_Acme_Corp.pdf",
  "mime": "application/pdf",
  "size": 4200,
  "checksum": "a3f2d8c9e1b7f4c6a2d5e8f1b4c7d0e3",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": [],
  "dueDate": null
}
```

**Response (400 Bad Request):**
```json
{
  "message": "File is empty"
}
```

**Response (413 Payload Too Large):**
```json
{
  "message": "File too large"
}
```

**Constraints:**
- Max file size: 10 MB
- File must not be empty
- File path must not contain `..` or `/` (security)

---

### Download file

```http
GET /api/files/{id}/download
```

**Path Parameters:**
- `{id}` (number, required) – File ID

**Response (200 OK):**
- Binary file content
- Header: `Content-Disposition: attachment; filename="..."`

**Response (404 Not Found):**
- File not found

**Example (curl):**
```bash
curl -o downloaded_file.pdf \
  http://localhost:8080/api/files/42/download
```

---

## Marker Management API

### Update markers (replace all)

```http
PATCH /api/files/{id}/markers
Content-Type: application/json
```

**Path Parameters:**
- `{id}` (number, required) – File ID

**Request Body:**
```json
{
  "markers": ["URGENT", "REVIEW"]
}
```

**Response (200 OK):**
```json
{
  "id": 42,
  "filename": "NDA_Acme_Corp.pdf",
  "mime": "application/pdf",
  "size": 4200,
  "checksum": "a3f2d8c9e1b7f4c6a2d5e8f1b4c7d0e3",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": ["URGENT", "REVIEW"],
  "dueDate": null
}
```

**Valid Marker Values:**
- `URGENT` – High priority, act immediately
- `REVIEW` – Needs review/signature
- `MISSING_INFO` – Information incomplete
- `INCOMPLETE_OCR` – OCR unsuccessful
- `FOLLOW_UP` – Follow-up required

**Notes:**
- Empty array `[]` removes all markers
- Duplicate markers are ignored
- Invalid marker names cause error

---

## Due Date Management API

### Update due date

```http
PATCH /api/files/{id}/due-date
Content-Type: application/json
```

**Path Parameters:**
- `{id}` (number, required) – File ID

**Request Body:**
```json
{
  "dueDate": "2024-12-31T00:00:00Z"
}
```

Or to clear/remove:
```json
{
  "dueDate": null
}
```

**Response (200 OK):**
```json
{
  "id": 42,
  "filename": "NDA_Acme_Corp.pdf",
  "mime": "application/pdf",
  "size": 4200,
  "checksum": "a3f2d8c9e1b7f4c6a2d5e8f1b4c7d0e3",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": ["URGENT", "REVIEW"],
  "dueDate": "2024-12-31T00:00:00Z"
}
```

**Format:**
- ISO 8601 date-time: `YYYY-MM-DDTHH:MM:SSZ`
- Null to clear: `null`

**Notes:**
- Dates are UTC/Zulu time
- No validation on past dates (server accepts any valid date)
- Frontend shows overdue dates in red

---

## Note Management API

### Update note

```http
PATCH /api/files/{id}/note
Content-Type: application/json
```

**Path Parameters:**
- `{id}` (number, required) – File ID

**Request Body:**
```json
{
  "note": "Signature required from CEO, sent 30.12.2024"
}
```

Or to clear:
```json
{
  "note": null
}
```

**Response (200 OK):**
```json
{
  "id": 42,
  "filename": "NDA_Acme_Corp.pdf",
  "mime": "application/pdf",
  "size": 4200,
  "checksum": "a3f2d8c9e1b7f4c6a2d5e8f1b4c7d0e3",
  "createdAt": "2024-12-30T15:50:00Z",
  "markers": ["URGENT", "REVIEW"],
  "dueDate": "2024-12-31T00:00:00Z",
  "note": "Signature required from CEO, sent 30.12.2024"
}
```

**Notes:**
- Max length: 65536 characters (CLOB)
- Empty string treated as null
- No markdown/formatting (plain text)

---

## Health & Monitoring APIs

### Health check

```http
GET /api/health
```

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

**Response (503 Service Unavailable):**
```json
{
  "status": "DOWN"
}
```

---

### Metrics (Micrometer/Prometheus)

```http
GET /api/actuator/metrics
```

**Response:**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.threads.live",
    "process.files.open",
    "http.server.requests",
    ...
  ]
}
```

---

### Prometheus metrics export

```http
GET /api/actuator/prometheus
```

**Response (text/plain):**
```
# HELP process_cpu_usage The current process CPU usage
# TYPE process_cpu_usage gauge
process_cpu_usage 0.2

# HELP jvm_memory_usage_bytes The amount of used memory
# TYPE jvm_memory_usage_bytes gauge
jvm_memory_usage_bytes{area="heap"} 250000000
jvm_memory_usage_bytes{area="nonheap"} 50000000
```

---

## Error Handling

### Common HTTP Status Codes

| Status | Meaning | Example |
|--------|---------|---------|
| 200 | OK – Request successful | File retrieved, updated, or uploaded |
| 400 | Bad Request – Invalid input | Empty file, invalid marker name |
| 404 | Not Found – Resource doesn't exist | File ID not found |
| 413 | Payload Too Large – File exceeds 10 MB | Upload too large |
| 500 | Internal Server Error – Server error | Database error, disk full |

### Error Response Format

```json
{
  "timestamp": "2024-12-30T15:50:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "File is empty"
}
```

---

## Pagination & Filtering

Currently **not supported** (MVP stage).

- `GET /api/files` returns **all files** (no pagination)
- Filtering must be done client-side (frontend)
- Future versions may add: `?limit=10&offset=0&marker=URGENT&sort=dueDate`

---

## Rate Limiting

Currently **not enforced** (MVP stage).

- No request limits
- No throttling
- Future versions may add rate limits per IP or user

---

## Webhooks

Currently **not supported** (MVP stage).

Future roadmap:
- Webhook events for file uploads
- Webhook events for marker/due-date changes
- Webhook events for overdue files

---

## Example Workflows

### Workflow 1: Upload, tag, and set deadline

```bash
# 1. Upload file
FILE_ID=$(curl -s -F "file=@contract.pdf" \
  http://localhost:8080/api/files/upload | jq -r '.id')

echo "Uploaded file with ID: $FILE_ID"

# 2. Add markers
curl -s -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"markers":["REVIEW"]}' \
  http://localhost:8080/api/files/$FILE_ID/markers

# 3. Set due date
curl -s -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"dueDate":"2025-01-15T00:00:00Z"}' \
  http://localhost:8080/api/files/$FILE_ID/due-date

# 4. Add note
curl -s -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"note":"Awaiting CEO signature"}' \
  http://localhost:8080/api/files/$FILE_ID/note

# 5. Get final result
curl -s http://localhost:8080/api/files/$FILE_ID | jq
```

### Workflow 2: Bulk update markers

```bash
# Get all files
curl -s http://localhost:8080/api/files | jq '.[] | select(.markers | length == 0) | .id' | while read ID; do
  echo "Adding URGENT marker to file $ID"
  curl -s -X PATCH \
    -H "Content-Type: application/json" \
    -d '{"markers":["URGENT"]}' \
    http://localhost:8080/api/files/$ID/markers
done
```

### Workflow 3: Export all files as JSON

```bash
curl -s http://localhost:8080/api/files > contracts_backup.json

# Pretty-print
curl -s http://localhost:8080/api/files | jq '.' > contracts_backup_pretty.json
```

---

## Environment Variables

Backend configuration via `.env`:

```env
# File storage
FILE_STORAGE_PATH=/data/files

# OCR Watcher
WATCH_DIR=/data/incoming
watcher.scan-interval-ms=5000
watcher.max-retries=5
watcher.backoff-ms=5000

# Database
SPRING_DATASOURCE_URL=jdbc:sqlite:mycontracts.db

# LLM (optional)
LLM_PROVIDER=gemini
GEMINI_API_KEY=your_key_here
```

---

## Changelog

### Version 1.0 (Current)
- ✅ File upload with checksum
- ✅ Multi-marker system (5 marker types)
- ✅ Due date tracking
- ✅ Notes/comments
- ✅ OCR watcher with retry logic
- ✅ Metrics & health endpoint
- ✅ React frontend with split-view

### Planned
- [ ] Tasks/Reminders tab (sort by due date)
- [ ] Bulk actions
- [ ] Contract linking (multiple files)
- [ ] Advanced OCR + LLM extraction
- [ ] Audit log
- [ ] Multi-user with roles
