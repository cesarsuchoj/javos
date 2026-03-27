import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { authService } from '../../services/authService'
import styles from './Header.module.css'

export default function Header() {
  const { name, role, refreshToken, logout } = useAuthStore()
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
        <span className={styles.tagline}>Sistema de Gestão</span>
      </div>
      <div className={styles.user}>
        <span className={styles.userName}>{name}</span>
        <span className={styles.userRole}>{role}</span>
        <button onClick={handleLogout} className={styles.logoutBtn}>
          Sair
        </button>
      </div>
    </header>
  )
}
