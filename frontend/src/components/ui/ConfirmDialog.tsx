import { useTranslation } from 'react-i18next'
import styles from './ConfirmDialog.module.css'

interface ConfirmDialogProps {
  message: string
  onConfirm: () => void
  onCancel: () => void
  loading?: boolean
}

export default function ConfirmDialog({
  message,
  onConfirm,
  onCancel,
  loading,
}: ConfirmDialogProps) {
  const { t } = useTranslation()

  return (
    <div className={styles.overlay} role="alertdialog" aria-modal="true">
      <div className={styles.dialog}>
        <p className={styles.message}>{message}</p>
        <div className={styles.actions}>
          <button className={styles.cancelBtn} onClick={onCancel} disabled={loading}>
            {t('common.cancel')}
          </button>
          <button className={styles.confirmBtn} onClick={onConfirm} disabled={loading}>
            {loading ? t('common.wait') : t('common.confirm')}
          </button>
        </div>
      </div>
    </div>
  )
}
