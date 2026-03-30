import { useTranslation } from 'react-i18next'
import styles from './LanguageSelector.module.css'

const LANGUAGES = [
  { code: 'pt-BR', label: 'PT' },
  { code: 'en', label: 'EN' },
  { code: 'es', label: 'ES' },
]

export default function LanguageSelector() {
  const { i18n, t } = useTranslation()

  const handleChange = (code: string) => {
    i18n.changeLanguage(code)
  }

  return (
    <div className={styles.selector} role="group" aria-label={t('language.label')}>
      {LANGUAGES.map((lang) => (
        <button
          key={lang.code}
          className={`${styles.btn} ${i18n.language === lang.code ? styles.active : ''}`}
          onClick={() => handleChange(lang.code)}
          aria-pressed={i18n.language === lang.code}
          title={t(`language.${lang.code}`)}
        >
          {lang.label}
        </button>
      ))}
    </div>
  )
}
