import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/authService'
import { useAuthStore } from '../../store/authStore'
import { getErrorMessage } from '../../services/api'
import { analyticsService } from '../../services/analyticsService'
import styles from './Login.module.css'

export default function Login() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { login } = useAuthStore()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({ username: false, password: false })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    const errors = { username: !username.trim(), password: !password.trim() }
    setFieldErrors(errors)
    if (errors.username) {
      setError(t('auth.usernameRequired'))
      return
    }
    if (errors.password) {
      setError(t('auth.passwordRequired'))
      return
    }
    setLoading(true)
    try {
      const response = await authService.login({ username, password })
      login(response.token, response.refreshToken, response.username, response.name, response.role)
      analyticsService.trackLogin(response.username)
      navigate('/dashboard')
    } catch (err) {
      analyticsService.trackError(getErrorMessage(err), 'login')
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h1 className={styles.title}>{t('auth.title')}</h1>
          <p className={styles.subtitle}>{t('auth.subtitle')}</p>
        </div>
        <form onSubmit={handleSubmit} className={styles.form}>
          {error && (
            <div className={styles.error} role="alert">
              {error}
            </div>
          )}
          <div className={styles.field}>
            <label htmlFor="username" className={styles.label}>
              {t('auth.username')}
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value)
                if (fieldErrors.username) setFieldErrors((f) => ({ ...f, username: false }))
              }}
              className={`${styles.input} ${fieldErrors.username ? styles.inputError : ''}`}
              placeholder={t('auth.usernamePlaceholder')}
              autoFocus
              aria-invalid={fieldErrors.username}
              aria-describedby={fieldErrors.username ? 'username-error' : undefined}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>
              {t('auth.password')}
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value)
                if (fieldErrors.password) setFieldErrors((f) => ({ ...f, password: false }))
              }}
              className={`${styles.input} ${fieldErrors.password ? styles.inputError : ''}`}
              placeholder={t('auth.passwordPlaceholder')}
              aria-invalid={fieldErrors.password}
              aria-describedby={fieldErrors.password ? 'password-error' : undefined}
            />
          </div>
          <button type="submit" className={styles.button} disabled={loading}>
            {loading ? (
              <>
                <span className={styles.spinner} aria-hidden="true" />
                {t('auth.loggingIn')}
              </>
            ) : (
              t('auth.login')
            )}
          </button>
        </form>
      </div>
    </div>
  )
}
