import { render, screen, fireEvent } from '@testing-library/react'
import { vi } from 'vitest'
import LanguageSelector from '../components/ui/LanguageSelector'

const mockChangeLanguage = vi.fn()

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'language.label': 'Idioma',
        'language.pt-BR': 'Português',
        'language.en': 'English',
        'language.es': 'Español',
      }
      return map[key] ?? key
    },
    i18n: {
      language: 'pt-BR',
      changeLanguage: mockChangeLanguage,
    },
  }),
}))

describe('LanguageSelector', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders PT, EN and ES buttons', () => {
    render(<LanguageSelector />)
    expect(screen.getByRole('button', { name: /PT/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /EN/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /ES/i })).toBeInTheDocument()
  })

  it('calls changeLanguage with "en" when EN is clicked', () => {
    render(<LanguageSelector />)
    fireEvent.click(screen.getByRole('button', { name: /EN/i }))
    expect(mockChangeLanguage).toHaveBeenCalledWith('en')
  })

  it('calls changeLanguage with "es" when ES is clicked', () => {
    render(<LanguageSelector />)
    fireEvent.click(screen.getByRole('button', { name: /ES/i }))
    expect(mockChangeLanguage).toHaveBeenCalledWith('es')
  })

  it('calls changeLanguage with "pt-BR" when PT is clicked', () => {
    render(<LanguageSelector />)
    fireEvent.click(screen.getByRole('button', { name: /PT/i }))
    expect(mockChangeLanguage).toHaveBeenCalledWith('pt-BR')
  })

  it('marks the active language button with aria-pressed="true"', () => {
    render(<LanguageSelector />)
    expect(screen.getByRole('button', { name: /PT/i })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: /EN/i })).toHaveAttribute('aria-pressed', 'false')
    expect(screen.getByRole('button', { name: /ES/i })).toHaveAttribute('aria-pressed', 'false')
  })

  it('renders with accessible group label', () => {
    render(<LanguageSelector />)
    expect(screen.getByRole('group', { name: 'Idioma' })).toBeInTheDocument()
  })
})
