import { useEffect, useState } from 'react'
import { dashboardService } from '../../services/dashboardService'
import { getErrorMessage } from '../../services/api'
import { DashboardSummary } from '../../types'
import styles from './Dashboard.module.css'

export default function Dashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    dashboardService
      .getSummary()
      .then(setSummary)
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div className={styles.loading}>Carregando...</div>
  }

  if (error) {
    return (
      <div className={styles.error} role="alert">
        {error}
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Resumo do Sistema</h2>
      <div className={styles.cards}>
        <div className={styles.card}>
          <span className={styles.cardLabel}>Total de Usuários</span>
          <span className={styles.cardValue}>{summary?.totalUsers ?? 0}</span>
        </div>
        <div className={styles.card}>
          <span className={styles.cardLabel}>Usuário Logado</span>
          <span className={styles.cardValue}>{summary?.loggedUser ?? '-'}</span>
        </div>
        <div className={styles.card}>
          <span className={styles.cardLabel}>Versão</span>
          <span className={styles.cardValue}>{summary?.version ?? '-'}</span>
        </div>
      </div>
    </div>
  )
}
