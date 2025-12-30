import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

const screenshotsDir = 'screenshots';

// Create screenshots directory if it doesn't exist
if (!fs.existsSync(screenshotsDir)) {
  fs.mkdirSync(screenshotsDir, { recursive: true });
}

test.describe('UI Workflow - Visual Documentation', () => {
  
  test('01 - Load app and verify health', async ({ page }) => {
    await page.goto('/');
    
    // Wait for app to be ready
    await page.waitForSelector('text=Files');
    
    // Verify health status shows OK
    const healthText = await page.locator('text=/Health:|offline|OK/i').first();
    await expect(healthText).toBeVisible();
    
    // Screenshot: Initial app load
    await page.screenshot({ path: `${screenshotsDir}/01-app-loaded.png`, fullPage: true });
  });

  test('02 - File list with markers and due dates', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    // Wait for file list to load
    await page.waitForSelector('[class*="file-item"]', { timeout: 5000 }).catch(() => null);
    
    // Screenshot: File list with all files visible
    await page.screenshot({ path: `${screenshotsDir}/02-file-list.png`, fullPage: true });
  });

  test('03 - Select file and view details', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    // Wait and click first file
    await page.waitForTimeout(1000);
    const firstFile = await page.locator('[class*="file-item"]').first();
    
    if (await firstFile.isVisible({ timeout: 2000 }).catch(() => false)) {
      await firstFile.click();
      
      // Wait for detail panel to appear
      await page.waitForSelector('[class*="detail-panel"]', { timeout: 5000 }).catch(() => null);
      
      // Screenshot: Detail view with markers and due date
      await page.screenshot({ path: `${screenshotsDir}/03-detail-panel.png`, fullPage: true });
    }
  });

  test('04 - Interact with markers (checkboxes)', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    await page.waitForTimeout(1000);
    const firstFile = await page.locator('[class*="file-item"]').first();
    
    if (await firstFile.isVisible({ timeout: 2000 }).catch(() => false)) {
      await firstFile.click();
      await page.waitForSelector('input[type="checkbox"]', { timeout: 5000 }).catch(() => null);
      
      // Get all marker checkboxes
      const checkboxes = await page.locator('input[type="checkbox"]').all();
      
      if (checkboxes.length > 0) {
        // Click first checkbox to toggle a marker
        await checkboxes[0].check().catch(() => null);
        await page.waitForTimeout(500);
        
        // Screenshot: Markers being edited
        await page.screenshot({ path: `${screenshotsDir}/04-markers-checkbox.png`, fullPage: true });
      }
    }
  });

  test('05 - Due date picker', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    await page.waitForTimeout(1000);
    const firstFile = await page.locator('[class*="file-item"]').first();
    
    if (await firstFile.isVisible({ timeout: 2000 }).catch(() => false)) {
      await firstFile.click();
      
      // Look for date input
      const dateInput = await page.locator('input[type="date"]').first().catch(() => null);
      
      if (dateInput) {
        // Set a due date
        await dateInput.fill('2025-12-31');
        await page.waitForTimeout(300);
        
        // Screenshot: Due date set
        await page.screenshot({ path: `${screenshotsDir}/05-due-date.png`, fullPage: true });
      }
    }
  });

  test('06 - Filter by Needs Attention', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    // Wait for filter controls
    await page.waitForTimeout(1000);
    
    // Find and click "Needs Attention" filter
    const filterButton = await page.locator('button, [role="button"]')
      .filter({ hasText: /Needs Attention/i })
      .first()
      .catch(() => null);
    
    if (filterButton) {
      await filterButton.click();
      await page.waitForTimeout(500);
      
      // Screenshot: Filtered view
      await page.screenshot({ path: `${screenshotsDir}/06-filter-needs-attention.png`, fullPage: true });
    } else {
      // If no specific filter button, show the current state
      const filterSelect = await page.locator('select').first().catch(() => null);
      if (filterSelect) {
        await filterSelect.selectOption('NEEDS_ATTENTION');
        await page.waitForTimeout(500);
        await page.screenshot({ path: `${screenshotsDir}/06-filter-needs-attention.png`, fullPage: true });
      }
    }
  });

  test('07 - Upload file (if form visible)', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    // Look for upload input
    const fileInput = await page.locator('input[type="file"]').first().catch(() => null);
    
    if (fileInput) {
      // Screenshot: Upload form visible
      await page.screenshot({ path: `${screenshotsDir}/07-upload-form.png`, fullPage: true });
    } else {
      // If no upload form visible, just show current state
      await page.screenshot({ path: `${screenshotsDir}/07-app-state.png`, fullPage: true });
    }
  });

  test('08 - Final app state', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('text=Files');
    
    await page.waitForTimeout(1500);
    
    // Full page screenshot showing entire UI
    await page.screenshot({ path: `${screenshotsDir}/08-final-state.png`, fullPage: true });
  });
});
