import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { dashboardService } from '../../services/dashboardService'
import { getErrorMessage } from '../../services/api'
import { DashboardSummary } from '../../types'
import styles from './Dashboard.module.css'

export default function Dashboard() {
  const { t } = useTranslation()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    dashboardService
      .getSummary()
      .then(setSummary)
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  if (loading) {
    return (
      <div className={styles.container}>
        <h2 className={styles.title}>{t('dashboard.title')}</h2>
        <div className={styles.cards}>
          {[1, 2, 3].map((i) => (
            <div key={i} className={styles.card}>
              <span className={styles.skeletonLabel} aria-hidden="true" />
              <span className={styles.skeletonValue} aria-hidden="true" />
            </div>
          ))}
        </div>
        <p className={styles.loading}>
          <span className={styles.spinner} aria-hidden="true" />
          {t('common.loading')}
        </p>
      </div>
    )
  }

  if (error) {
    return (
      <div className={styles.container}>
        <h2 className={styles.title}>{t('dashboard.title')}</h2>
        <div className={styles.error} role="alert">
          {error}
          <button onClick={load} className={styles.retryBtn}>
            {t('dashboard.retry')}
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>{t('dashboard.title')}</h2>
      <div className={styles.cards}>
        <div className={styles.card}>
          <span className={styles.cardLabel}>{t('dashboard.totalUsers')}</span>
          <span className={styles.cardValue}>{summary?.totalUsers ?? 0}</span>
        </div>
        <div className={styles.card}>
          <span className={styles.cardLabel}>{t('dashboard.loggedUser')}</span>
          <span className={styles.cardValue}>{summary?.loggedUser ?? '-'}</span>
        </div>
        <div className={styles.card}>
          <span className={styles.cardLabel}>{t('dashboard.version')}</span>
          <span className={styles.cardValue}>{summary?.version ?? '-'}</span>
        </div>
      </div>
    </div>
  )
}
