import { test, expect } from '@playwright/test';

test('diagram lookup keeps exact part selection and signed page context through the review form', async ({ page }) => {
  await page.goto('/backflow-preventer-diagram/');
  await page.selectOption('#desk-model', 'wilkins-975xl');
  await page.selectOption('#desk-size', '3/4–1 in');
  await page.selectOption('#desk-scope', 'Complete internal service');
  await expect(page.locator('[data-desk-matches] .fv-part-row')).toHaveCount(1);
  await expect(page.locator('[data-desk-matches]')).toContainText('RK34-975XLC');
  await expect(page.locator('[data-desk-matches] [data-link-kind="merchant"]')).toHaveAttribute('data-part-number', 'RK34-975XLC');
  await page.locator('[data-desk-results] [data-desk-help]').click();
  await expect(page.locator('select[name="issueType"]')).toHaveValue('parts-check');
  await expect(page.locator('textarea[name="notes"]')).toHaveValue(/RK34-975XLC/);
  await expect(page.locator('input[name="sourcePage"]')).toHaveValue('/backflow-preventer-diagram/');
  await expect(page.locator('input[name="pageFamily"]')).toHaveValue('decision-guide');
  await expect(page.locator('input[name="rt"]')).toHaveValue(/.+/);
  await expect(page.locator('[data-support-context]')).toBeVisible();
  expect(await page.locator('input[name="phone"]').evaluate(e => e.required)).toBe(false);
  await page.locator('input[name="email"]').fill('review@example.com');
  expect(await page.locator('[data-support-form]').evaluate(form => form.checkValidity())).toBe(false); // consent still required
  await page.locator('input[name="consentToRouting"]').check();
  expect(await page.locator('[data-support-form]').evaluate(form => form.checkValidity())).toBe(true);
});

test('installation brief reflects job constraints and carries them into an installation review', async ({ page }) => {
  await page.goto('/backflow-preventer-installation/');
  await expect(page.locator('#installation-tab')).toHaveAttribute('aria-selected', 'true');
  await page.selectOption('#brief-system', 'Fire protection');
  await page.selectOption('#brief-place', 'Indoors or in a mechanical room');
  await page.getByRole('button', { name: 'Build my brief' }).click();
  await expect(page.locator('[data-brief-list]')).toContainText('fire authority');
  await expect(page.locator('[data-brief-list]')).toContainText('relief-discharge capacity');
  await expect(page.locator('[data-brief-output]')).toBeVisible();
  await page.locator('[data-desk-help="installation"]').click();
  await expect(page.locator('select[name="issueType"]')).toHaveValue('installation');
  await expect(page.locator('textarea[name="notes"]')).toHaveValue(/Fire protection/);
  await expect(page.locator('textarea[name="notes"]')).toHaveValue(/report filer/);
});

test('field controls work by keyboard, remain inside mobile width, and do not leak brief text to analytics', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/backflow-preventer-diagram/');
  await page.locator('#parts-tab').focus();
  await page.keyboard.press('ArrowRight');
  await expect(page.locator('#installation-tab')).toBeFocused();
  await expect(page.locator('#installation-panel')).toBeVisible();
  await page.evaluate(() => { window.__events = []; window.gtag = (...args) => window.__events.push(args); });
  await page.getByRole('button', { name: 'Build my brief' }).click();
  const events = await page.evaluate(() => window.__events);
  expect(events.some(e => e[1] === 'installation_brief_created')).toBe(true);
  expect(JSON.stringify(events)).not.toContain('BACKFLOW INSTALLER BRIEF');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await page.locator('#parts-tab').click();
  await page.selectOption('#desk-model', 'wilkins-975xl');
  await page.selectOption('#desk-size', '3/4–1 in');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
});

test('field reference and support routes remain usable without JavaScript', async ({ browser }) => {
  const context = await browser.newContext({ javaScriptEnabled: false });
  const page = await context.newPage();
  await page.goto('/backflow-preventer-diagram/');
  await expect(page.getByRole('link', { name: 'Browse all model and size references without JavaScript' })).toBeVisible();
  await expect(page.locator('[data-desk-panel="installation"]')).toBeVisible();
  await expect(page.locator('[data-desk-help="parts-check"]').first()).toHaveAttribute('href', /source=.*backflow-preventer-diagram/);
  await page.goto('/backflow-preventer-installation/');
  await expect(page.locator('[data-installation-detail]')).toBeVisible();
  await context.close();
});

test('manufacturer drawings are available before selecting a size and unknown-model photos keep the source context', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/backflow-preventer-diagram/');
  await page.selectOption('#desk-model', 'wilkins-975xl');
  await expect(page.locator('[data-desk-documents]')).toBeVisible();
  await expect(page.locator('[data-desk-documents]')).toContainText('component matrix and exploded drawing');
  await expect(page.locator('#desk-size')).toHaveValue('');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await page.locator('[data-identify-help]').click();
  await expect(page.locator('select[name="issueType"]')).toHaveValue('model-identification');
  await expect(page.locator('textarea[name="notes"]')).toHaveValue(/MODEL IDENTIFICATION REQUEST/);
  const tinyPng = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jR1EAAAAASUVORK5CYII=', 'base64');
  await page.locator('[data-photo-input]').setInputFiles({name:'label.png',mimeType:'image/png',buffer:tinyPng});
  await expect(page.locator('[data-photo-previews] img')).toHaveCount(1);
  await expect(page.locator('[data-photo-status]')).toContainText('Nothing uploads until you send');
  await page.getByRole('button',{name:'Remove photo 1'}).click();
  await expect(page.locator('[data-photo-previews] img')).toHaveCount(0);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
});

test('FEBCO scope selection exposes manufacturer-matched numeric ordering code and retailer route', async ({ page }) => {
  await page.goto('/backflow-preventer-diagram/');
  await page.selectOption('#desk-model','febco-765');
  await page.selectOption('#desk-size','1/2–3/4 in');
  await page.selectOption('#desk-scope','Bonnet and poppet');
  await expect(page.locator('[data-desk-matches]')).toContainText('905211');
  await expect(page.locator('[data-desk-matches] [data-link-kind="merchant"]')).toHaveAttribute('href',/Febco-905211-/);
  await expect(page.locator('[data-desk-matches]')).not.toContainText('905212');
});
