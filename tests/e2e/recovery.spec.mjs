import { test, expect } from '@playwright/test';

test('kit selection exposes the exact size, scope and merchant without mixing sources', async ({ page }) => {
  await page.goto('/models/wilkins-975xl/');
  await page.selectOption('[data-kit-size]', '3/4–1 in');
  await page.selectOption('[data-kit-task]', 'Complete internal service');
  const row = page.locator('[data-kit-option]:visible');
  await expect(row).toHaveCount(1);
  await expect(row).toContainText('RK34-975XLC');
  await expect(row).toContainText('do not assume seats are included');
  await expect(row.locator('[data-link-kind="merchant"]')).toHaveAttribute('data-part-number', 'RK34-975XLC');
  await page.selectOption('[data-kit-size]', '1 1/4–2 in');
  await expect(page.locator('[data-kit-option]:visible')).toHaveCount(3);
  expect((await page.locator('[data-kit-option]:visible').allTextContents()).every(text => !text.includes('RK34-'))).toBe(true);
});

test('part references remain accessible without JavaScript', async ({ browser }) => {
  const context = await browser.newContext({ javaScriptEnabled: false });
  const page = await context.newPage();
  await page.goto('/models/wilkins-975xl/');
  await expect(page.getByText('RK34-975XLC', { exact: true })).toBeVisible();
  await page.goto('/backflow-preventer-repair-kits/');
  await expect(page.locator('[data-global-option]').first()).toBeVisible();
  await context.close();
});

test('analytics distinguishes manufacturer and merchant clicks and strips query data', async ({ page }) => {
  await page.route('https://www.googletagmanager.com/**', route => route.abort());
  await page.goto('/models/wilkins-975xl/');
  await page.evaluate(() => { window.__events = []; window.gtag = (...args) => window.__events.push(args); });
  await page.evaluate(() => document.addEventListener('click', e => e.preventDefault()));
  await page.locator('#sources [data-link-kind="manufacturer"]').first().click();
  await page.selectOption('[data-kit-size]', '3/4–1 in');
  await page.locator('[data-kit-option]:visible [data-link-kind="merchant"]').first().click();
  const events = await page.evaluate(() => window.__events);
  expect(events.filter(e => e[1] === 'manufacturer_source_click')).toHaveLength(1);
  expect(events.filter(e => e[1] === 'merchant_outbound_click')).toHaveLength(1);
  expect(events.filter(e => e[1] === 'provider_website_click')).toHaveLength(0);
  expect(JSON.stringify(events)).not.toContain('https://');
  await page.goto('/leads/thanks');
  expect(await page.evaluate(() => Array.from(window.dataLayer || []).some(e => e[1] === 'lead_form_success'))).toBe(false);
});

test('eight recovery URLs stay indexable and fit a mobile viewport', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  for (const path of ['/backflow-preventer-installation/', '/backflow-preventer-diagram/', '/vacuum-breaker-leaking/', '/double-check-valve-leaking/', '/backflow-preventer-leaking/', '/models/wilkins-975xl/', '/models/febco-825y/', '/backflow-preventer-repair-kits/']) {
    await page.goto(path);
    await expect(page.locator('h1')).toHaveCount(1);
    await expect(page.locator('meta[name="robots"]')).toHaveAttribute('content', /^index,follow/);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  }
});
