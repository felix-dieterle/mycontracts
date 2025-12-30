# UI & Testing Guide

## Automated UI Tests with Playwright

This project includes automated UI tests using Playwright that capture screenshots throughout the application workflow.

### Setup

Tests are configured in `playwright.config.ts` with the following settings:
- Single worker (sequential tests)
- Screenshot capture on all tests
- HTML test report generation
- Supports both headless and headed modes

### Running Tests

```bash
# Run all tests (headless)
npm run test:ui

# Run tests in headed mode (watch browser)
npm run test:ui:headed

# Run tests in debug mode (step through)
npm run test:ui:debug
```

### Test Coverage

The test suite includes the following scenarios:

#### Test 1: App Load
- Verifies initial app loads correctly
- Takes screenshot of initial UI state
- File: `screenshots/01-app-loaded.png`

#### Test 2: File List
- Displays loaded files with markers and due dates
- Shows file list with visual markers
- File: `screenshots/02-file-list.png`

#### Test 3: Detail Panel
- Clicks a file to open detail view
- Shows file detail panel with editing controls
- File: `screenshots/03-detail-panel.png`

#### Test 4: Marker Interaction
- Interacts with marker checkboxes
- Updates marker selections
- File: `screenshots/04-markers.png`

#### Test 5: Due Date Editing
- Sets/updates due date using date picker
- Demonstrates date input interaction
- File: `screenshots/05-due-date.png`

#### Test 6: Final State
- Captures overall app state after interactions
- Full page screenshot showing complete UI
- File: `screenshots/06-final-state.png`

### Screenshot Gallery

Screenshots are automatically saved to the `screenshots/` directory:

- **01-app-loaded.png** - Initial app load with file list
- **02-file-list.png** - File list with all sample files
- **03-detail-panel.png** - Detail panel for selected file
- **04-markers.png** - Marker checkbox interactions
- **05-due-date.png** - Due date picker usage
- **06-final-state.png** - Complete app state after edits

### Test Results

After running tests, view the HTML report:

```bash
npx playwright show-report
```

This opens the full test report in your browser with:
- Test execution timeline
- Screenshots for each step
- Video recordings (if failures)
- Detailed trace logs

### Configuration

Key settings in `playwright.config.ts`:

```typescript
timeout: 60 * 1000,                    // 60 second timeout per test
screenshot: 'always',                  // Capture every test
video: 'retain-on-failure',           // Videos only on failure
navigationTimeout: 30000,              // 30 second nav timeout
actionTimeout: 10000,                  // 10 second action timeout
reuseExistingServer: true,            // Use running dev server
```

### Troubleshooting

**Tests timing out:**
- Ensure backend is running on port 8080
- Ensure frontend dev server is running on port 5173
- Check browser availability: `npx playwright install`

**Screenshots not captured:**
- Verify `screenshots/` directory exists
- Check file permissions
- Run with `--debug` flag for more info

**Tests can't reach app:**
- Verify frontend is accessible: `curl http://localhost:5173`
- Verify backend is accessible: `curl http://localhost:8080/api/health`
- Check that `baseURL` in config matches frontend URL

### Using Screenshots in Documentation

To include test screenshots in README or other docs:

```markdown
![App loaded](frontend/screenshots/01-app-loaded.png)
![File list](frontend/screenshots/02-file-list.png)
![Detail panel](frontend/screenshots/03-detail-panel.png)
```

### Advanced Usage

#### Run specific test
```bash
npx playwright test ui-workflow.spec.ts
```

#### Run with specific browser
```bash
npx playwright test --project=chromium
```

#### Generate report with custom name
```bash
npx playwright test --output=my-test-results
```

### CI/CD Integration

To integrate these tests into your CI pipeline, add to your workflow:

```yaml
- name: Install Playwright
  run: npx playwright install --with-deps

- name: Run UI tests
  run: npm run test:ui
  working-directory: ./frontend

- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: playwright-report
    path: frontend/test-results/
    retention-days: 30
```

### Notes

- Tests run sequentially (workers: 1) to ensure stable screenshot order
- All selectors are flexible to handle different DOM structures
- Tests use generous timeouts (30s nav, 10s actions) for slower environments
- Failures automatically capture video for debugging
