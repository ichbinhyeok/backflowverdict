import { test, expect } from '@playwright/test';

test('diagnostic works by keyboard and announces the result', async ({ page }) => {
  await page.goto('/vacuum-breaker-leaking/');
  const firstChoice = page.locator('[data-tool-choice]').first();
  await firstChoice.focus();
  await page.keyboard.press('Enter');
  const result = page.locator('[data-tool-result]');
  await expect(result).toBeVisible();
  await expect(result).toBeFocused();
  await expect(result).toHaveAttribute('aria-live', 'polite');
  await expect(result.locator('[data-result-title]')).not.toBeEmpty();
});

test('reduced motion uses an instant result transition', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.addInitScript(() => {
    Element.prototype.scrollIntoView = function (options) { window.__scrollBehavior = options.behavior; };
  });
  await page.goto('/vacuum-breaker-leaking/');
  await page.locator('[data-tool-choice]').first().click();
  await expect.poll(() => page.evaluate(() => window.__scrollBehavior)).toBe('auto');
});

test('pressure tool validates inputs and returns a focused interpretation', async ({ page }) => {
  await page.goto('/water-pressure-regulator-adjustment/');
  await page.getByLabel('Static pressure before use').fill('82');
  await page.getByLabel('Pressure with a fixture running').fill('58');
  await page.getByRole('button', { name: 'Interpret the readings' }).click();
  const result = page.locator('[data-pressure-tool] [data-tool-result]');
  await expect(result).toBeVisible();
  await expect(result).toBeFocused();
  await expect(result).toContainText('above the common residential code threshold');
});

test('cost screen builds sourced scope without inventing dollar ranges', async ({ page }) => {
  await page.goto('/water-pressure-regulator-cost/');
  await page.selectOption('[name="access"]', { label: 'Wall, pit, or slab work' });
  await page.selectOption('[name="scope"]', { label: 'Commercial or permit scope' });
  await page.getByRole('button', { name: 'Build a quote checklist' }).click();
  const result = page.locator('[data-cost-tool] [data-tool-result]');
  await expect(result).toContainText('Wall, pit, or slab work');
  await expect(result).toContainText('does not invent a national dollar range');
  await expect(result).not.toContainText(/\$\d/);
});

test('utility selection composes the official record into the decision screen', async ({ page }) => {
  await page.goto('/backflow-test/');
  await page.selectOption('#utility-query', 'lee-county-utilities');
  await page.getByRole('button', { name: 'Apply official rule' }).click();
  await expect(page).toHaveURL(/utility=lee-county-utilities/);
  await expect(page.locator('.bv-utility-overlay')).toContainText('Lee County');
  await expect(page.locator('.bv-utility-overlay')).toContainText('Last verified');
  await expect(page.locator('.bv-utility-overlay a[rel="noopener noreferrer"]')).toHaveAttribute('href', /^https:\/\//);
});

test('published decision assets are indexable', async ({ page }) => {
  await page.goto('/backflow-preventer-installation/');
  await expect(page.locator('meta[name="robots"]')).toHaveAttribute('content', /^index,follow/);
  await page.goto('/water-pressure-regulator-symptoms/');
  await expect(page.locator('h1')).toBeVisible();
  await expect(page.locator('meta[name="robots"]')).toHaveAttribute('content', /^index,follow/);
});
