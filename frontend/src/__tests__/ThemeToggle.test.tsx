import { render, screen, fireEvent } from '@testing-library/react'
import { vi, beforeEach, describe, it, expect } from 'vitest'

// ── Mock react-i18next ──────────────────────────────────────────────────────
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'theme.switchToDark': 'Switch to dark mode',
        'theme.switchToLight': 'Switch to light mode',
        'header.tagline': 'Management System',
        'header.logout': 'Sign Out',
        'language.label': 'Language',
        'language.pt-BR': 'Português',
        'language.en': 'English',
        'language.es': 'Español',
      }
      return map[key] ?? key
    },
    i18n: { language: 'en', changeLanguage: vi.fn() },
  }),
}))

// ── Mock react-router-dom ───────────────────────────────────────────────────
vi.mock('react-router-dom', () => ({
  useNavigate: () => vi.fn(),
}))

// ── Mock authStore ──────────────────────────────────────────────────────────
vi.mock('../store/authStore', () => ({
  useAuthStore: () => ({
    name: 'Admin',
    role: 'ROLE_ADMIN',
    refreshToken: null,
    logout: vi.fn(),
  }),
}))

// ── Mock authService ────────────────────────────────────────────────────────
vi.mock('../services/authService', () => ({
  authService: { logout: vi.fn() },
}))

// ── themeStore mock ─────────────────────────────────────────────────────────
const themeState = { theme: 'light' as 'light' | 'dark', toggle: vi.fn() }

vi.mock('../store/themeStore', () => ({
  useThemeStore: () => themeState,
  applyTheme: vi.fn(),
}))

import Header from '../components/layout/Header'

describe('Theme toggle in Header', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    themeState.theme = 'light'
    themeState.toggle = vi.fn()
  })

  it('renders the moon icon when theme is light', () => {
    render(<Header />)
    const btn = screen.getByRole('button', { name: /Switch to dark mode/i })
    expect(btn).toBeInTheDocument()
    expect(btn.textContent).toBe('🌙')
  })

  it('renders the sun icon when theme is dark', () => {
    themeState.theme = 'dark'
    render(<Header />)
    const btn = screen.getByRole('button', { name: /Switch to light mode/i })
    expect(btn).toBeInTheDocument()
    expect(btn.textContent).toBe('☀️')
  })

  it('calls toggle when the button is clicked', () => {
    render(<Header />)
    fireEvent.click(screen.getByRole('button', { name: /Switch to dark mode/i }))
    expect(themeState.toggle).toHaveBeenCalledTimes(1)
  })
})

