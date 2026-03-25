import Header from '../components/layout/Header'
import Sidebar from '../components/layout/Sidebar'
import Dashboard from '../components/dashboard/Dashboard'
import styles from './DashboardPage.module.css'

export default function DashboardPage() {
  return (
    <div className={styles.layout}>
      <Header />
      <div className={styles.body}>
        <Sidebar />
        <main className={styles.main}>
          <Dashboard />
        </main>
      </div>
    </div>
  )
}
