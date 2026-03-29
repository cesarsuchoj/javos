import { test, expect } from '@playwright/test';

/**
 * Smoke tests: verify the application is reachable and renders the login page.
 */
test.describe('Application smoke tests', () => {
  test('serves the application root', async ({ page }) => {
    const response = await page.goto('/');
    expect(response?.status()).toBeLessThan(500);
  });

  test('login page is visible', async ({ page }) => {
    await page.goto('/');
    // The login form has a username (text) input and a password input
    await expect(page.locator('input[type="password"]')).toBeVisible({
      timeout: 10000,
    });
  });
});
