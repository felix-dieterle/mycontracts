#!/bin/bash
# Test Execution Recipes - Copy & Paste Ready

# =============================================================================
# SETUP (One-time, ~2 minutes)
# =============================================================================

# Install Playwright browsers (required before first test run)
cd /workspaces/mycontracts/frontend && npx playwright install --with-deps


# =============================================================================
# ENSURE SERVICES ARE RUNNING (in separate terminals)
# =============================================================================

# Terminal 1: Start Backend
cd /workspaces/mycontracts/backend
mvn spring-boot:run -DskipTests

# Terminal 2: Start Frontend  
cd /workspaces/mycontracts/frontend
npm run dev -- --host --port 5173


# =============================================================================
# RUN TESTS (choose one)
# =============================================================================

# OPTION A: Run all tests silently, create screenshots
cd /workspaces/mycontracts/frontend
npm run test:ui

# OPTION B: Run with visible browser (slower, watchable)
npm run test:ui:headed

# OPTION C: Debug mode (pause and inspect each step)
npm run test:ui:debug


# =============================================================================
# VIEW RESULTS
# =============================================================================

# List generated screenshots
ls -lh /workspaces/mycontracts/frontend/screenshots/

# Open full test report in browser
cd /workspaces/mycontracts/frontend
npx playwright show-report


# =============================================================================
# TROUBLESHOOTING
# =============================================================================

# Kill stuck processes
pkill -f "npm\|vite\|playwright\|java"
sleep 2

# Check if services respond
curl http://localhost:8080/api/health     # Backend health
curl http://localhost:5173                # Frontend loads

# Clean and reinstall browsers
cd /workspaces/mycontracts/frontend
rm -rf .playwright
npx playwright install --with-deps


# =============================================================================
# ADVANCED
# =============================================================================

# Run single test file
npx playwright test tests/ui-workflow.spec.ts

# Run specific test by name
npx playwright test -g "Load app"

# Run with different browser
npx playwright test --project=chromium

# Generate custom report
npx playwright test --output=my-results

# Show browser console output
PLAYWRIGHT_HEADLESS=0 npm run test:ui:headed

# Save detailed trace for debugging
npx playwright test --trace on


# =============================================================================
# CI/CD INTEGRATION
# =============================================================================

# For GitHub Actions, use:
# npx playwright install --with-deps
# npm run test:ui
# Results: frontend/test-results/
#          frontend/screenshots/


# =============================================================================
# EXPECTED OUTPUTS
# =============================================================================

# Screenshots directory:
# frontend/screenshots/
# ├── 01-app-loaded.png
# ├── 02-file-list.png
# ├── 03-detail-panel.png
# ├── 04-markers.png
# ├── 05-due-date.png
# └── 06-final-state.png

# HTML Report:
# frontend/test-results/
# ├── index.html (open in browser)
# └── trace files, videos, etc.


# =============================================================================
# QUICK ALIASES (add to ~/.bashrc or run directly)
# =============================================================================

alias test-ui="cd /workspaces/mycontracts/frontend && npm run test:ui"
alias test-ui-headed="cd /workspaces/mycontracts/frontend && npm run test:ui:headed"
alias test-report="cd /workspaces/mycontracts/frontend && npx playwright show-report"
alias screenshots="ls -lh /workspaces/mycontracts/frontend/screenshots/"

# Usage: test-ui
