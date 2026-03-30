import { NavLink } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import styles from './Sidebar.module.css'

export default function Sidebar() {
  const { t } = useTranslation()

  const navItems = [
    { path: '/dashboard', label: t('nav.dashboard'), icon: '📊' },
    { path: '/clients', label: t('nav.clients'), icon: '👥' },
    { path: '/products', label: t('nav.products'), icon: '📦' },
    { path: '/service-orders', label: t('nav.serviceOrders'), icon: '🔧' },
    { path: '/financial', label: t('nav.financial'), icon: '💰' },
  ]

  return (
    <aside className={styles.sidebar}>
      <nav className={styles.nav}>
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `${styles.navItem} ${isActive ? styles.active : ''}`
            }
          >
            <span className={styles.icon}>{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
