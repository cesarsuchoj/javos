import { test, expect } from '@playwright/test';
import { setAuthState, mockDataEndpoints, mockLogoutEndpoint } from './helpers/auth';

test.describe('Sidebar navigation', () => {
  test.beforeEach(async ({ page }) => {
    await mockDataEndpoints(page);
    await mockLogoutEndpoint(page);
    await setAuthState(page);
  });

  test('navigates to Dashboard', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.locator('nav a[href="/dashboard"]')).toBeVisible();
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('navigates to Clients page', async ({ page }) => {
    await page.goto('/dashboard');
    await page.locator('nav a[href="/clients"]').click();
    await page.waitForURL('**/clients');
    await expect(page.locator('h2', { hasText: 'Clientes' })).toBeVisible();
  });

  test('navigates to Products page', async ({ page }) => {
    await page.goto('/dashboard');
    await page.locator('nav a[href="/products"]').click();
    await page.waitForURL('**/products');
    await expect(page.locator('h2', { hasText: 'Produtos' })).toBeVisible();
  });

  test('navigates to Service Orders page', async ({ page }) => {
    await page.goto('/dashboard');
    await page.locator('nav a[href="/service-orders"]').click();
    await page.waitForURL('**/service-orders');
    await expect(page.locator('h2', { hasText: 'Ordens de Serviço' })).toBeVisible();
  });

  test('navigates to Financial page', async ({ page }) => {
    await page.goto('/dashboard');
    await page.locator('nav a[href="/financial"]').click();
    await page.waitForURL('**/financial');
    await expect(page.locator('h2', { hasText: 'Financeiro' })).toBeVisible();
  });

  test('root path redirects to /dashboard', async ({ page }) => {
    await page.goto('/');
    await page.waitForURL('**/dashboard');
    expect(page.url()).toContain('/dashboard');
  });

  test('unknown path redirects to /dashboard', async ({ page }) => {
    await page.goto('/this-route-does-not-exist');
    await page.waitForURL('**/dashboard');
    expect(page.url()).toContain('/dashboard');
  });

  test('header shows logged-in user name and role', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.locator('header')).toContainText('Administrador');
  });
});
