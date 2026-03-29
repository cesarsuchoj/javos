import { Page, Route } from '@playwright/test';

/** Fake JWT-shaped token used across all mocked auth flows. */
export const FAKE_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.e30.fake';
export const FAKE_REFRESH = 'fake-refresh-token';

export const MOCK_USER = {
  token: FAKE_TOKEN,
  refreshToken: FAKE_REFRESH,
  type: 'Bearer',
  username: 'admin',
  name: 'Administrador',
  role: 'ROLE_ADMIN',
  expiresIn: 3600,
};

/** Fixed ISO timestamp used in mock API responses for deterministic test output. */
const MOCK_TIMESTAMP = '2024-01-01T00:00:00.000Z';

/**
 * Registers a mock for the login endpoint so no real backend is required.
 * Calls to POST /api/v1/auth/login with valid credentials return a 200,
 * invalid credentials return a 401.
 */
export async function mockLoginEndpoint(page: Page): Promise<void> {
  await page.route('**/api/v1/auth/login', (route: Route) => {
    const body = route.request().postDataJSON() as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'admin123') {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(MOCK_USER) });
    } else {
      route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ status: 401, message: 'Unauthorized', timestamp: MOCK_TIMESTAMP }),
      });
    }
  });
}

/**
 * Registers a mock for the logout endpoint.
 */
export async function mockLogoutEndpoint(page: Page): Promise<void> {
  await page.route('**/api/v1/auth/logout', (route: Route) => {
    route.fulfill({ status: 200 });
  });
}

/**
 * Simulates a logged-in session by writing the auth state directly into
 * localStorage (same key used by the Zustand persist middleware).
 * This bypasses the login page so tests can jump straight to protected routes.
 */
export async function setAuthState(page: Page): Promise<void> {
  await page.goto('/');
  await page.evaluate((user) => {
    const state = {
      state: {
        token: user.token,
        refreshToken: user.refreshToken,
        username: user.username,
        name: user.name,
        role: user.role,
      },
      version: 0,
    };
    localStorage.setItem('javos-auth', JSON.stringify(state));
  }, MOCK_USER);
}

/**
 * Mocks all data endpoints that authenticated pages rely on so the app
 * renders without a real backend.
 */
export async function mockDataEndpoints(page: Page): Promise<void> {
  await page.route('**/api/v1/clients**', (route: Route) => {
    if (route.request().method() === 'GET') {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    } else {
      route.continue();
    }
  });
  await page.route('**/api/v1/products**', (route: Route) => {
    if (route.request().method() === 'GET') {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    } else {
      route.continue();
    }
  });
  await page.route('**/api/v1/service-orders**', (route: Route) => {
    if (route.request().method() === 'GET') {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    } else {
      route.continue();
    }
  });
  await page.route('**/api/v1/dashboard**', (route: Route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ totalUsers: 1, loggedUser: 'admin', version: '1.0.0' }),
    });
  });
  await page.route('**/api/v1/financial/**', (route: Route) => {
    if (route.request().method() === 'GET') {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    } else {
      route.continue();
    }
  });
}
