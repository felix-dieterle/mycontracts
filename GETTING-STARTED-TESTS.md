# 🚀 Getting Started - UI Testing

## TL;DR - 3 Commands

```bash
# 1. Install browsers (first time only)
cd /workspaces/mycontracts/frontend && npx playwright install --with-deps

# 2. Ensure backend/frontend are running
# Backend: http://localhost:8080
# Frontend: http://localhost:5173

# 3. Generate screenshots
cd /workspaces/mycontracts/frontend && npm run test:ui
```

**Done!** Screenshots are in `frontend/screenshots/`

---

## What Just Happened?

You now have a complete **Playwright testing framework** that:

✅ **Automatically tests your app UI**  
✅ **Takes screenshots at each step**  
✅ **Generates HTML reports**  
✅ **Works in CI/CD pipelines**  

## Test Coverage

| # | Test | Screenshot |
|---|------|-----------|
| 1 | App loads | `01-app-loaded.png` |
| 2 | File list | `02-file-list.png` |
| 3 | Detail panel | `03-detail-panel.png` |
| 4 | Markers | `04-markers.png` |
| 5 | Due dates | `05-due-date.png` |
| 6 | Final state | `06-final-state.png` |

## Available Commands

```bash
npm run test:ui          # Run tests (fast, headless)
npm run test:ui:headed   # Run with browser visible
npm run test:ui:debug    # Step through with debugger
npx playwright show-report    # View HTML test report
```

## File Locations

| What | Where |
|------|-------|
| Test config | `frontend/playwright.config.ts` |
| Test code | `frontend/tests/ui-workflow.spec.ts` |
| Screenshots | `frontend/screenshots/` (created when tests run) |
| Reports | `frontend/test-results/` (created when tests run) |

## Documentation

Start with: **[TESTING-INDEX.md](TESTING-INDEX.md)**

Quick guide: **[QUICK-START-TESTS.md](QUICK-START-TESTS.md)**

Full reference: **[TESTING.md](TESTING.md)**

## Common Questions

**Q: How long do tests take?**  
A: ~1-2 minutes for all 6 tests

**Q: Can I use these screenshots in documentation?**  
A: Yes! That's the whole point. Include in README.md or other docs.

**Q: Do I need to click anything?**  
A: No! Tests are fully automated.

**Q: What if tests fail?**  
A: Check the HTML report: `npx playwright show-report`

**Q: Can I run a single test?**  
A: Yes! `npx playwright test -g "Load app"`

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Tests hang | `pkill -f "npm\|vite\|playwright"` and restart services |
| "Browser not found" | `npx playwright install --with-deps` |
| "Cannot reach app" | Verify backend/frontend are running |

## Next Steps

1. ✅ **Install browsers** - `npx playwright install --with-deps`
2. ✅ **Run tests** - `npm run test:ui`
3. ✅ **Check screenshots** - `ls frontend/screenshots/`
4. ✅ **Use in docs** - Include in README.md

---

**Status**: ✅ Framework ready to use!

Need more help? See [TESTING-INDEX.md](TESTING-INDEX.md) for full documentation.
