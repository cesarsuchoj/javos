import styles from '../../pages/crud.module.css'

export default function PageLoader() {
  return (
    <div className={styles.loading}>
      <span className={styles.spinner} />
    </div>
  )
}
