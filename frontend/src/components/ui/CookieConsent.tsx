import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { analyticsService } from '../../services/analyticsService'
import styles from './CookieConsent.module.css'

/**
 * Bandeira de consentimento de cookies (GDPR/LGPD).
 *
 * Exibida apenas enquanto o status de consentimento for "pending".
 * Ao aceitar ou recusar, a preferência é armazenada no localStorage
 * e o banner desaparece permanentemente.
 */
export default function CookieConsent() {
  const { t } = useTranslation()
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    setVisible(analyticsService.getConsentStatus() === 'pending')
  }, [])

  const handleAccept = () => {
    analyticsService.grantConsent()
    setVisible(false)
  }

  const handleDeny = () => {
    analyticsService.denyConsent()
    setVisible(false)
  }

  if (!visible) return null

  return (
    <div className={styles.banner} role="dialog" aria-label={t('cookieConsent.title')}>
      <div className={styles.content}>
        <p className={styles.text}>{t('cookieConsent.message')}</p>
        <div className={styles.actions}>
          <button onClick={handleDeny} className={styles.denyButton}>
            {t('cookieConsent.deny')}
          </button>
          <button onClick={handleAccept} className={styles.acceptButton}>
            {t('cookieConsent.accept')}
          </button>
        </div>
      </div>
    </div>
  )
}
