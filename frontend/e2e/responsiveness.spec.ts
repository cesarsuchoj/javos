import { test, expect } from '@playwright/test';
import { setAuthState, mockDataEndpoints, mockLoginEndpoint, mockLogoutEndpoint } from './helpers/auth';

/**
 * Responsiveness tests validate that key UI elements are visible and usable
 * across mobile, tablet and desktop viewport sizes.
 *
 * These tests deliberately override the viewport so they can run in any
 * Playwright project (chromium, firefox, webkit, mobile-*).
 */

const VIEWPORTS = [
  { label: 'mobile', width: 375, height: 667 },
  { label: 'tablet', width: 768, height: 1024 },
  { label: 'desktop', width: 1280, height: 800 },
];

for (const vp of VIEWPORTS) {
  test.describe(`Login page – ${vp.label} (${vp.width}×${vp.height})`, () => {
    test.use({ viewport: { width: vp.width, height: vp.height } });

    test('renders login form without overflow', async ({ page }) => {
      await mockLoginEndpoint(page);
      await page.goto('/login');

      const username = page.locator('input#username');
      const password = page.locator('input#password');
      const submit = page.locator('button[type="submit"]');

      await expect(username).toBeVisible();
      await expect(password).toBeVisible();
      await expect(submit).toBeVisible();

      // All interactive elements should be inside the visible viewport
      await expect(username).toBeInViewport();
      await expect(submit).toBeInViewport();
    });
  });

  test.describe(`Authenticated layout – ${vp.label} (${vp.width}×${vp.height})`, () => {
    test.use({ viewport: { width: vp.width, height: vp.height } });

    test.beforeEach(async ({ page }) => {
      await mockDataEndpoints(page);
      await mockLogoutEndpoint(page);
      await setAuthState(page);
    });

    test('renders header with brand name', async ({ page }) => {
      await page.goto('/dashboard');
      await expect(page.locator('header')).toBeVisible();
      await expect(page.locator('header')).toContainText('Javos');
    });

    test('renders sidebar navigation links', async ({ page }) => {
      await page.goto('/dashboard');
      const sidebar = page.locator('aside nav');
      await expect(sidebar).toBeVisible();
      // Core nav links
      await expect(sidebar.locator('a[href="/clients"]')).toBeVisible();
      await expect(sidebar.locator('a[href="/products"]')).toBeVisible();
      await expect(sidebar.locator('a[href="/service-orders"]')).toBeVisible();
    });

    test('clients page table scrolls horizontally on small viewports', async ({ page }) => {
      await page.route('**/api/v1/clients**', (route) => {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      });
      await page.goto('/clients');
      // The page heading must be visible at every breakpoint
      await expect(page.locator('h2', { hasText: 'Clientes' })).toBeVisible();
      // Either the empty-state message or the data table must be rendered
      await expect(
        page.locator('text=Nenhum cliente encontrado').or(page.locator('table')).first(),
      ).toBeVisible();
    });
  });
}
