import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
import Login from '../components/auth/Login'
import * as authServiceModule from '../services/authService'
import { useAuthStore } from '../store/authStore'

// Mock react-router-dom navigate
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

// Mock auth service
vi.mock('../services/authService', () => ({
  authService: {
    login: vi.fn(),
  },
}))

// Mock authStore
vi.mock('../store/authStore', () => ({
  useAuthStore: vi.fn(),
}))

// Mock getErrorMessage
vi.mock('../services/api', () => ({
  getErrorMessage: (err: unknown) => (err instanceof Error ? err.message : 'Erro ao conectar'),
}))

function renderLogin() {
  return render(
    <MemoryRouter>
      <Login />
    </MemoryRouter>,
  )
}

describe('Login', () => {
  const mockLogin = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    ;(useAuthStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue({ login: mockLogin })
  })

  it('renders the login form', () => {
    renderLogin()
    expect(screen.getByRole('heading', { name: 'Javos' })).toBeInTheDocument()
    expect(screen.getByLabelText('Usuário')).toBeInTheDocument()
    expect(screen.getByLabelText('Senha')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('shows error when submitting empty username', async () => {
    renderLogin()
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('O campo usuário é obrigatório.')
    })
  })

  it('shows error when submitting empty password', async () => {
    renderLogin()
    await userEvent.type(screen.getByLabelText('Usuário'), 'testuser')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('O campo senha é obrigatório.')
    })
  })

  it('calls authService.login with username and password', async () => {
    const mockAuthService = authServiceModule.authService as { login: ReturnType<typeof vi.fn> }
    mockAuthService.login.mockResolvedValueOnce({
      token: 'access-token',
      refreshToken: 'refresh-token',
      username: 'testuser',
      name: 'Test User',
      role: 'ROLE_USER',
    })

    renderLogin()
    await userEvent.type(screen.getByLabelText('Usuário'), 'testuser')
    await userEvent.type(screen.getByLabelText('Senha'), 'password123')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => {
      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      })
    })
  })

  it('navigates to /dashboard on successful login', async () => {
    const mockAuthService = authServiceModule.authService as { login: ReturnType<typeof vi.fn> }
    mockAuthService.login.mockResolvedValueOnce({
      token: 'access-token',
      refreshToken: 'refresh-token',
      username: 'testuser',
      name: 'Test User',
      role: 'ROLE_USER',
    })

    renderLogin()
    await userEvent.type(screen.getByLabelText('Usuário'), 'testuser')
    await userEvent.type(screen.getByLabelText('Senha'), 'password123')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
    })
  })

  it('shows error message on login failure', async () => {
    const mockAuthService = authServiceModule.authService as { login: ReturnType<typeof vi.fn> }
    mockAuthService.login.mockRejectedValueOnce(new Error('Credenciais inválidas'))

    renderLogin()
    await userEvent.type(screen.getByLabelText('Usuário'), 'testuser')
    await userEvent.type(screen.getByLabelText('Senha'), 'wrongpassword')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Credenciais inválidas')
    })
  })

  it('disables submit button while loading', async () => {
    const mockAuthService = authServiceModule.authService as { login: ReturnType<typeof vi.fn> }
    mockAuthService.login.mockReturnValue(new Promise(() => {})) // never resolves

    renderLogin()
    await userEvent.type(screen.getByLabelText('Usuário'), 'testuser')
    await userEvent.type(screen.getByLabelText('Senha'), 'password123')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Entrando/ })).toBeDisabled()
    })
  })
})
