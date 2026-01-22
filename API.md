# mycontracts – REST API Reference

## Base URL

```
http://localhost:8080/api
```

## Authentication

Currently **no authentication** required (MVP stage).

---

## Widget API

### Get widget status

Get a snapshot of savegame metrics and metadata for Android widget display.

```http
GET /api/widget/status
```

**Parameters:** None

**Response (200 OK):**
```json
{
  "timestamp": "2026-01-22T15:33:42.415Z",
  "totalFiles": 3,
  "needsAttention": 2,
  "overdueCount": 1,
  "urgentCount": 1,
  "upcomingDueDates30Days": 2,
  "ocrPending": 0,
  "ocrFailed": 0,
  "ocrMatched": 0,
  "missingInfo": 1,
  "needsCategorization": 1,
  "recentFiles": [
    {
      "id": 3,
      "filename": "contract3.pdf",
      "createdAt": "2026-01-22T15:33:38.199Z",
      "markers": [],
      "ocrStatus": null,
      "dueDate": null
    },
    {
      "id": 2,
      "filename": "contract2.pdf",
      "createdAt": "2026-01-22T15:33:38.182Z",
      "markers": ["MISSING_INFO"],
      "ocrStatus": null,
      "dueDate": "2026-02-15T00:00:00Z"
    },
    {
      "id": 1,
      "filename": "test_contract.pdf",
      "createdAt": "2026-01-22T15:33:13.885Z",
      "markers": ["URGENT", "REVIEW"],
      "ocrStatus": null,
      "dueDate": "2026-01-20T00:00:00Z"
    }
  ],
  "recommendations": [
    "🔴 1 überfällige Verträge prüfen",
    "🟣 1 Verträge mit unvollständigen Informationen vervollständigen",
    "📝 1 Verträge kategorisieren und Fälligkeiten setzen"
  ]
}
```

**Response fields:**
- `timestamp` – Time when this snapshot was generated
- `totalFiles` – Total number of files/contracts in the system
- `needsAttention` – Files with URGENT/REVIEW/MISSING_INFO markers or overdue
- `overdueCount` – Files with due date in the past
- `urgentCount` – Files marked as URGENT
- `upcomingDueDates30Days` – Files with due date within next 30 days
- `ocrPending` – Files with OCR status PENDING
- `ocrFailed` – Files with OCR status FAILED
- `ocrMatched` – Files with OCR status MATCHED
- `missingInfo` – Files marked with MISSING_INFO
- `needsCategorization` – Files without markers, due date, or notes
- `recentFiles` – List of up to 5 most recently created files
- `recommendations` – List of actionable recommendations based on current state

**Use case:**
This endpoint is designed for Android widgets or mobile apps to quickly retrieve
the current state of all contracts without needing to fetch and process the full
list. It provides pre-calculated metrics and recommendations for display.

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

## AI & Analysis APIs

### Chat with AI

```http
POST /api/ai/chat
Content-Type: application/json

{
  "messages": [
    {
      "role": "user",
      "content": "What are the main risks in this contract?"
    }
  ],
  "fileId": 1
}
```

**Description:**
Start a chat with AI about a contract. The AI has access to the file's metadata and OCR data.

**Request Parameters:**
- `messages` (array, required) – Chat message history
  - `role` (string) – Either "user" or "assistant"
  - `content` (string) – Message content
- `fileId` (number, optional) – File ID for context

**Response (200 OK):**
```json
{
  "message": "Based on the contract context, the main risks include: 1) Unlimited liability exposure...",
  "role": "assistant",
  "error": false
}
```

**Response (400 Bad Request):**
```json
{
  "message": "OpenRouter API is not configured. Please set OPENROUTER_API_KEY.",
  "role": "assistant",
  "error": true
}
```

---

### Optimize Contract

```http
POST /api/ai/optimize
Content-Type: application/json

{
  "fileId": 1
}
```

**Description:**
Get AI-powered optimization suggestions for a contract, including risk analysis and improvement recommendations.

**Request Parameters:**
- `fileId` (number, required) – File ID to analyze

**Response (200 OK):**
```json
{
  "suggestions": [
    "Add termination clause with 30-day notice period",
    "Include liability cap at contract value"
  ],
  "risks": [
    "Unlimited liability exposure",
    "No termination clause"
  ],
  "improvements": [
    "Define clear deliverables and milestones",
    "Add payment schedule details"
  ],
  "summary": "Detailed analysis of the contract with specific recommendations..."
}
```

---

### Analyze OCR Data

```http
POST /api/ai/analyze-ocr
Content-Type: application/json

{
  "fileId": 1
}
```

**Description:**
Analyzes OCR data from a file and extracts standard contract fields using AI. The extracted fields are automatically stored in the database and linked to a contract. This is particularly useful for insurance contracts and other standardized documents.

**Request Parameters:**
- `fileId` (number, required) – File ID with OCR data to analyze

**Response (200 OK):**
```json
{
  "success": true,
  "contractId": 5,
  "extractedFields": {
    "description": "Life insurance policy with savings component",
    "cost_per_month": "€250.00",
    "cost_per_year": "€3,000.00",
    "return_on_death": "€100,000",
    "return_on_quitting": "€15,000",
    "payment_hold_option": "yes",
    "current_value": "€18,500",
    "contract_type": "life insurance",
    "provider": "Allianz Lebensversicherung AG",
    "contract_number": "LV-123456789",
    "start_date": "2020-01-15",
    "end_date": "2045-01-15",
    "cancellation_period": "3 months",
    "coverage_amount": "€100,000"
  },
  "fieldCount": 14
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "error": "No OCR data found for file ID: 1"
}
```

**Extracted Fields:**
The AI extracts the following standard fields when available:
- `description` – Brief summary of the contract
- `cost_per_month` – Monthly premium/cost
- `cost_per_year` – Annual premium/cost
- `return_on_death` – Death benefit amount
- `return_on_quitting` – Surrender value or early termination payout
- `payment_hold_option` – Whether payments can be paused (yes/no)
- `current_value` – Current cash value or policy value
- `contract_type` – Type (life insurance, health insurance, property insurance, etc.)
- `provider` – Insurance company or provider name
- `contract_number` – Policy or contract number
- `start_date` – Contract start date
- `end_date` – Contract end or maturity date
- `cancellation_period` – Notice period for cancellation
- `coverage_amount` – Total insured amount or coverage

**Notes:**
- Requires OCR data to be available for the file (status: MATCHED)
- Uses AI (LLM) to parse and extract structured information
- Extracted fields are stored with confidence score 0.8 and source "LLM"
- Creates a new Contract if the file doesn't have one linked

---

### Web Search for Contract Info

```http
POST /api/ai/web-search
Content-Type: application/json

{
  "fileId": 1,
  "query": "Allianz life insurance reviews"
}
```

**Description:**
Performs a web search to find additional information about a contract, insurance provider, or specific product. Uses AI models with web search capabilities (e.g., Perplexity AI) to provide up-to-date information from the internet.

**Request Parameters:**
- `fileId` (number, required) – File ID to search information for
- `query` (string, optional) – Custom search query. If not provided, a query will be generated from file metadata

**Response (200 OK):**
```json
{
  "success": true,
  "query": "Allianz life insurance reviews",
  "results": "Allianz is one of the world's leading insurance and asset management companies...\n\nCustomer Reviews:\n- Generally positive ratings (4.2/5 stars)\n- Known for reliable claim processing\n- Competitive pricing in the German market\n\nRecent Updates:\n- New digital services launched in 2024\n- Enhanced customer portal features",
  "fileId": 1,
  "filename": "allianz_contract.pdf"
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "error": "OpenRouter API is not configured. Please set OPENROUTER_API_KEY."
}
```

**Notes:**
- Uses AI models with online search capabilities (default: Perplexity AI)
- Provides recent, up-to-date information from the web
- Results include company background, reviews, pricing, and features
- Query is auto-generated from filename if not provided

---

### Search and Analyze Contract

```http
POST /api/ai/search-and-analyze
Content-Type: application/json

{
  "fileId": 1,
  "query": "Allianz life insurance comparison"
}
```

**Description:**
Performs a comprehensive analysis by combining file metadata with web search results. This provides detailed information including provider background, market comparison, risk identification, and optimization recommendations.

**Request Parameters:**
- `fileId` (number, required) – File ID to analyze
- `query` (string, optional) – Custom search query for additional research

**Response (200 OK):**
```json
{
  "success": true,
  "query": "Allianz life insurance comparison",
  "analysis": "## Provider Background\nAllianz is a leading German insurance company with over 130 years of experience...\n\n## Market Comparison\nCompared to similar products:\n- Premium rates: Competitive (middle range)\n- Coverage options: Above average\n- Customer satisfaction: 4.2/5 stars\n\n## Identified Risks\n- Premium increases after initial period\n- Limited flexibility in payment adjustments\n\n## Recommendations\n1. Review premium increase schedule\n2. Consider additional riders for comprehensive coverage\n3. Compare with HUK-Coburg and AXA alternatives\n\n## Alternative Options\n- HUK-Coburg: Lower premiums, similar coverage\n- AXA: Higher premiums, more flexibility",
  "fileId": 1,
  "filename": "allianz_contract.pdf"
}
```

**Analysis Includes:**
1. Provider/company background and reputation
2. Contract type and features
3. Market comparison (pricing, terms, benefits)
4. Potential risks or concerns
5. Recommendations for optimization
6. Alternative options if available

**Notes:**
- Combines local file data with web-based research
- Uses Perplexity AI or similar models with online search
- Provides actionable insights for contract optimization
- Results are current as of the search time

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
