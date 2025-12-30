import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

const screenshotsDir = 'screenshots';

// Create screenshots directory if it doesn't exist
if (!fs.existsSync(screenshotsDir)) {
  fs.mkdirSync(screenshotsDir, { recursive: true });
}

test.describe('UI Workflow - Visual Documentation', () => {
  
  test('01 - Load app and verify UI loads', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle' });
    await page.waitForTimeout(2000);
    
    // Screenshot: Initial app load
    await page.screenshot({ path: `${screenshotsDir}/01-app-loaded.png`, fullPage: true });
  });

  test('02 - File list visible', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(3000);
    
    // Screenshot: File list
    await page.screenshot({ path: `${screenshotsDir}/02-file-list.png`, fullPage: true });
  });

  test('03 - Select first file', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(3000);
    
    // Try to click any file link
    const files = await page.locator('a, button, [role="button"]').all();
    if (files.length > 0) {
      await files[0].click({ timeout: 5000 }).catch(() => null);
      await page.waitForTimeout(1000);
    }
    
    // Screenshot: Detail panel
    await page.screenshot({ path: `${screenshotsDir}/03-detail-panel.png`, fullPage: true });
  });

  test('04 - Interact with markers', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(3000);
    
    // Look for checkboxes
    const checkboxes = await page.locator('input[type="checkbox"]').all();
    if (checkboxes.length > 0) {
      await checkboxes[0].check({ timeout: 5000 }).catch(() => null);
      await page.waitForTimeout(500);
    }
    
    // Screenshot: Markers interaction
    await page.screenshot({ path: `${screenshotsDir}/04-markers.png`, fullPage: true });
  });

  test('05 - Set due date', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(3000);
    
    // Look for date input
    const dateInputs = await page.locator('input[type="date"]').all();
    if (dateInputs.length > 0) {
      await dateInputs[0].fill('2025-12-31', { timeout: 5000 }).catch(() => null);
      await page.waitForTimeout(500);
    }
    
    // Screenshot: Due date set
    await page.screenshot({ path: `${screenshotsDir}/05-due-date.png`, fullPage: true });
  });

  test('06 - Final app state', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(3000);
    
    // Full page screenshot
    await page.screenshot({ path: `${screenshotsDir}/06-final-state.png`, fullPage: true });
  });
});
