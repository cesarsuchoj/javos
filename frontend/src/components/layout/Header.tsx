import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuthStore } from '../../store/authStore'
import { useThemeStore } from '../../store/themeStore'
import { authService } from '../../services/authService'
import LanguageSelector from '../ui/LanguageSelector'
import styles from './Header.module.css'

export default function Header() {
  const { t } = useTranslation()
  const { name, role, refreshToken, logout } = useAuthStore()
  const { theme, toggle } = useThemeStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    if (refreshToken) {
      try {
        await authService.logout(refreshToken)
      } catch {
        // proceed with local logout even if server call fails
      }
    }
    logout()
    navigate('/login')
  }

  return (
    <header className={styles.header}>
      <div className={styles.brand}>
        <span className={styles.logo}>Javos</span>
        <span className={styles.tagline}>{t('header.tagline')}</span>
      </div>
      <div className={styles.user}>
        <LanguageSelector />
        <button
          onClick={toggle}
          className={styles.themeToggle}
          aria-label={t(theme === 'dark' ? 'theme.switchToLight' : 'theme.switchToDark')}
          title={t(theme === 'dark' ? 'theme.switchToLight' : 'theme.switchToDark')}
        >
          {theme === 'dark' ? '☀️' : '🌙'}
        </button>
        <span className={styles.userName}>{name}</span>
        <span className={styles.userRole}>{role}</span>
        <button onClick={handleLogout} className={styles.logoutBtn}>
          {t('header.logout')}
        </button>
      </div>
    </header>
  )
}
