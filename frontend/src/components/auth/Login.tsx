import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService } from '../../services/authService'
import { useAuthStore } from '../../store/authStore'
import { getErrorMessage } from '../../services/api'
import styles from './Login.module.css'

export default function Login() {
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
      setError('O campo usuário é obrigatório.')
      return
    }
    if (errors.password) {
      setError('O campo senha é obrigatório.')
      return
    }
    setLoading(true)
    try {
      const response = await authService.login({ username, password })
      login(response.token, response.refreshToken, response.username, response.name, response.role)
      navigate('/dashboard')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h1 className={styles.title}>Javos</h1>
          <p className={styles.subtitle}>Sistema de Gestão</p>
        </div>
        <form onSubmit={handleSubmit} className={styles.form}>
          {error && (
            <div className={styles.error} role="alert">
              {error}
            </div>
          )}
          <div className={styles.field}>
            <label htmlFor="username" className={styles.label}>
              Usuário
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
              placeholder="Digite seu usuário"
              autoFocus
              aria-invalid={fieldErrors.username}
              aria-describedby={fieldErrors.username ? 'username-error' : undefined}
            />
          </div>
          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>
              Senha
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
              placeholder="Digite sua senha"
              aria-invalid={fieldErrors.password}
              aria-describedby={fieldErrors.password ? 'password-error' : undefined}
            />
          </div>
          <button type="submit" className={styles.button} disabled={loading}>
            {loading ? (
              <>
                <span className={styles.spinner} aria-hidden="true" />
                Entrando...
              </>
            ) : (
              'Entrar'
            )}
          </button>
        </form>
      </div>
    </div>
  )
}
