import { test, expect, Route } from '@playwright/test';
import { setAuthState, mockLogoutEndpoint } from './helpers/auth';

const MOCK_CLIENT = {
  id: 1,
  name: 'João Silva',
  email: 'joao@example.com',
  phone: '11999990000',
  document: '123.456.789-00',
  address: 'Rua A, 100',
  city: 'São Paulo',
  state: 'SP',
  zipCode: '01310-100',
  active: true,
  notes: '',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

const UPDATED_CLIENT = { ...MOCK_CLIENT, name: 'João Silva Atualizado', email: 'joao.new@example.com' };

test.describe('Clients CRUD', () => {
  test.beforeEach(async ({ page }) => {
    await mockLogoutEndpoint(page);

    // Mock other data endpoints so the app renders without backend
    await page.route('**/api/v1/products**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.route('**/api/v1/service-orders**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.route('**/api/v1/dashboard**', (route: Route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ totalUsers: 1, loggedUser: 'admin', version: '1.0.0' }),
      });
    });
    await page.route('**/api/v1/financial/**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });

    await setAuthState(page);
  });

  test('shows empty state when no clients exist', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.goto('/clients');
    await expect(page.locator('text=Nenhum cliente encontrado')).toBeVisible();
  });

  test('lists existing clients', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([MOCK_CLIENT]) });
    });
    await page.goto('/clients');
    await expect(page.locator('td', { hasText: 'João Silva' }).first()).toBeVisible();
    await expect(page.locator('td', { hasText: 'joao@example.com' }).first()).toBeVisible();
  });

  test('opens create modal when clicking "Novo Cliente"', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.goto('/clients');
    await page.locator('button', { hasText: '+ Novo Cliente' }).click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    await expect(page.locator('input#client-name')).toBeVisible();
  });

  test('shows form validation error when name is empty', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.goto('/clients');
    await page.locator('button', { hasText: '+ Novo Cliente' }).click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    // Remove native HTML5 `required` so the React submit handler runs and
    // shows the application-level validation message.  The `required` attribute
    // causes the browser to block the submit event before React's handler fires,
    // so this manipulation is intentional: we are testing React's validation
    // layer, not the browser's built-in constraint validation.
    await page.locator('input#client-name').evaluate((el: HTMLInputElement) => {
      el.required = false;
    });
    await page.locator('[role="dialog"] button[type="submit"]').click();
    await expect(page.locator('text=O nome é obrigatório')).toBeVisible();
  });

  test('creates a new client successfully', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      const method = route.request().method();
      if (method === 'POST') {
        route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify(MOCK_CLIENT) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      }
    });

    await page.goto('/clients');
    await page.locator('button', { hasText: '+ Novo Cliente' }).click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    await page.locator('input#client-name').fill('João Silva');
    await page.locator('input#client-email').fill('joao@example.com');
    await page.locator('[role="dialog"] button[type="submit"]').click();

    await expect(page.locator('[role="status"]')).toContainText('Cliente criado com sucesso');
  });

  test('opens edit modal with pre-filled data', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([MOCK_CLIENT]) });
    });
    await page.goto('/clients');
    await page.locator('button', { hasText: 'Editar' }).first().click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    await expect(page.locator('input#client-name')).toHaveValue('João Silva');
  });

  test('edits a client successfully', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      const method = route.request().method();
      if (method === 'PUT') {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(UPDATED_CLIENT) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([MOCK_CLIENT]) });
      }
    });

    await page.goto('/clients');
    await page.locator('button', { hasText: 'Editar' }).first().click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    await page.locator('input#client-name').fill('João Silva Atualizado');
    await page.locator('[role="dialog"] button[type="submit"]').click();

    await expect(page.locator('[role="status"]')).toContainText('Cliente atualizado com sucesso');
  });

  test('shows delete confirmation dialog', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([MOCK_CLIENT]) });
    });
    await page.goto('/clients');
    await page.locator('button', { hasText: 'Excluir' }).first().click();
    await expect(page.locator('text=Deseja excluir o cliente')).toBeVisible();
  });

  test('deletes a client successfully', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      const method = route.request().method();
      if (method === 'DELETE') {
        route.fulfill({ status: 204 });
      } else {
        // Always return the client for any GET so the 'Excluir' button is visible.
        // After deletion the clientStore filters local state without re-fetching.
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([MOCK_CLIENT]) });
      }
    });

    await page.goto('/clients');
    await page.locator('button', { hasText: 'Excluir' }).first().click();
    await page.locator('button', { hasText: 'Confirmar' }).click();

    await expect(page.locator('[role="status"]')).toContainText('excluído com sucesso');
  });

  test('cancels delete dialog without deleting', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([MOCK_CLIENT]) });
    });
    await page.goto('/clients');
    await page.locator('button', { hasText: 'Excluir' }).first().click();
    await page.locator('button', { hasText: 'Cancelar' }).click();
    // Client row is still visible
    await expect(page.locator('td', { hasText: 'João Silva' }).first()).toBeVisible();
  });

  test('filters clients by name using search', async ({ page }) => {
    const clients = [
      MOCK_CLIENT,
      { ...MOCK_CLIENT, id: 2, name: 'Maria Souza', email: 'maria@example.com', document: '987.654.321-00' },
    ];
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(clients) });
    });

    await page.goto('/clients');
    // Both clients visible initially
    await expect(page.locator('td', { hasText: 'João Silva' }).first()).toBeVisible();
    await expect(page.locator('td', { hasText: 'Maria Souza' }).first()).toBeVisible();

    // Filter by "Maria"
    await page.locator('input[type="search"]').fill('Maria');
    await expect(page.locator('td', { hasText: 'Maria Souza' }).first()).toBeVisible();
    await expect(page.locator('td', { hasText: 'João Silva' })).toHaveCount(0);
  });

  test('shows error alert when API returns server error', async ({ page }) => {
    await page.route('**/api/v1/clients**', (route: Route) => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ status: 500, message: 'Internal Server Error', timestamp: new Date().toISOString() }),
      });
    });
    await page.goto('/clients');
    await expect(page.locator('[role="alert"]')).toBeVisible();
  });
});
