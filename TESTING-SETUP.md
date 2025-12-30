# Setup Complete - UI Testing Framework

## What Was Added

### 1. Playwright Configuration
- File: `frontend/playwright.config.ts`
- Sets up test environment with:
  - Chromium browser
  - HTML report generation
  - Screenshot capture for all tests
  - 60-second test timeout
  - Auto-reuse of existing dev server

### 2. UI Test Suite
- File: `frontend/tests/ui-workflow.spec.ts`
- 6 comprehensive test scenarios:
  1. App loads successfully
  2. File list displays with markers & due dates
  3. Detail panel opens for selected files
  4. Marker checkboxes interact correctly
  5. Due date picker sets dates
  6. Final app state after edits

### 3. Package.json Scripts
Added three new test commands:
```json
{
  "test:ui": "playwright test",
  "test:ui:headed": "playwright test --headed",
  "test:ui:debug": "playwright test --debug"
}
```

### 4. Documentation
- File: `TESTING.md` - Complete testing guide
  - How to run tests
  - What each test covers
  - Troubleshooting guide
  - CI/CD integration examples
  - How to use screenshots in docs

- Updated `README.md`
  - Added testing section
  - Links to TESTING.md
  - Quick start commands

## How to Run Tests

### First Time Setup
```bash
cd frontend
npm install  # Already done
npx playwright install --with-deps  # Install browsers
```

### Run Tests
```bash
# Headless (default)
npm run test:ui

# With browser visible
npm run test:ui:headed

# Interactive debug mode
npm run test:ui:debug

# View test report
npx playwright show-report
```

## What Tests Do

Each test:
1. Navigates to the app
2. Waits for elements to load
3. Interacts with UI (clicks, types, etc.)
4. Takes a screenshot of the result
5. Saves screenshot to `screenshots/` directory

Screenshots are automatically saved as:
- `01-app-loaded.png` - Initial state
- `02-file-list.png` - File list view
- `03-detail-panel.png` - Detail panel
- `04-markers.png` - Marker interactions
- `05-due-date.png` - Date picker
- `06-final-state.png` - Final state

## Expected Output

After running tests, you get:
1. **Screenshots folder** - `frontend/screenshots/` with all test images
2. **HTML Report** - `frontend/test-results/index.html` with:
   - Test execution details
   - Screenshots at each step
   - Video recordings of failures
   - Performance traces
3. **Console output** - Shows pass/fail for each test

## Requirements for Tests to Work

✅ **Backend** running on `http://localhost:8080`
✅ **Frontend** dev server on `http://localhost:5173`
✅ **Browsers** installed via `playwright install`
✅ **Node.js** 16+ (for npm/npx)

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Tests hang/timeout | Kill old processes: `pkill -f "npm\|vite"` then restart |
| "Cannot find browser" | Run: `npx playwright install --with-deps` |
| Tests can't reach app | Check: `curl http://localhost:5173` and `curl http://localhost:8080` |
| Screenshots not created | Verify `frontend/screenshots/` directory exists and has write permissions |
| Permission denied on screenshots | Run: `chmod 755 frontend/screenshots/` |

## Next Steps

1. **Install browsers**:
   ```bash
   cd /workspaces/mycontracts/frontend
   npx playwright install --with-deps
   ```

2. **Start services**:
   ```bash
   # Terminal 1: Backend
   cd backend
   mvn spring-boot:run -DskipTests
   
   # Terminal 2: Frontend
   cd frontend
   npm run dev -- --host --port 5173
   ```

3. **Run tests**:
   ```bash
   cd frontend
   npm run test:ui
   ```

4. **View results**:
   ```bash
   # Opens HTML report in browser
   npx playwright show-report
   ```

## Using Screenshots in Documentation

Once tests run and create screenshots, you can use them in docs:

```markdown
### File List View
![File list with markers](frontend/screenshots/02-file-list.png)

### Editing Markers
![Marker interaction](frontend/screenshots/04-markers.png)

### Setting Due Dates
![Due date picker](frontend/screenshots/05-due-date.png)
```

## GitHub Actions Integration

To run tests in CI, add to `.github/workflows/test.yml`:

```yaml
- name: Install Playwright
  run: npx playwright install --with-deps
  working-directory: ./frontend

- name: Run UI tests
  run: npm run test:ui
  working-directory: ./frontend

- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: frontend/test-results/
```

---

**Status**: ✅ Framework setup complete, ready to run tests!
