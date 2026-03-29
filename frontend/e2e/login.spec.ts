import { test, expect } from '@playwright/test';
import { mockLoginEndpoint, mockLogoutEndpoint, setAuthState, mockDataEndpoints } from './helpers/auth';

test.describe('Login page', () => {
  test.beforeEach(async ({ page }) => {
    await mockLoginEndpoint(page);
  });

  test('renders the login form', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input#username')).toBeVisible();
    await expect(page.locator('input#password')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('shows error when submitting with empty username', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input#password').fill('somepassword');
    await page.locator('button[type="submit"]').click();
    await expect(page.locator('[role="alert"]')).toContainText('usuário é obrigatório');
  });

  test('shows error when submitting with empty password', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input#username').fill('admin');
    await page.locator('button[type="submit"]').click();
    await expect(page.locator('[role="alert"]')).toContainText('senha é obrigatório');
  });

  test('shows error on invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input#username').fill('wrong');
    await page.locator('input#password').fill('wrong');
    await page.locator('button[type="submit"]').click();
    await expect(page.locator('[role="alert"]')).toContainText('Usuário ou senha incorretos');
  });

  test('redirects to dashboard on successful login', async ({ page }) => {
    await mockDataEndpoints(page);
    await page.goto('/login');
    await page.locator('input#username').fill('admin');
    await page.locator('input#password').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await page.waitForURL('**/dashboard');
    expect(page.url()).toContain('/dashboard');
  });

  test('logs out and returns to login page', async ({ page }) => {
    await mockLogoutEndpoint(page);
    await mockDataEndpoints(page);
    await setAuthState(page);
    await page.goto('/dashboard');
    await page.locator('button', { hasText: 'Sair' }).click();
    await page.waitForURL('**/login');
    expect(page.url()).toContain('/login');
  });
});

test.describe('Login page – unauthenticated redirects', () => {
  test('redirects unauthenticated users from /dashboard to /login', async ({ page }) => {
    await page.goto('/dashboard');
    await page.waitForURL('**/login');
    expect(page.url()).toContain('/login');
  });

  test('redirects unauthenticated users from /clients to /login', async ({ page }) => {
    await page.goto('/clients');
    await page.waitForURL('**/login');
    expect(page.url()).toContain('/login');
  });
});
