import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useProductStore } from '../store/productStore'
import { Product, ProductRequest, ProductType } from '../types'
import { getErrorMessage } from '../services/api'
import { useNotification } from '../hooks/useNotification'
import { formatCurrency } from '../i18n/locale'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import styles from './crud.module.css'

const emptyForm: ProductRequest = {
  code: '',
  name: '',
  description: '',
  type: 'PRODUCT',
  price: 0,
  cost: 0,
  stockQty: 0,
  unit: '',
  active: true,
}

export default function ProductsPage() {
  const { t } = useTranslation()
  const { products, loading, error, fetchAll, create, update, remove, clearError } =
    useProductStore()

  const { notification, notify, clearNotification } = useNotification()
  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Product | null>(null)
  const [form, setForm] = useState<ProductRequest>(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<Product | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchAll()
  }, [fetchAll])

  const filtered = products.filter(
    (p) =>
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      (p.code ?? '').toLowerCase().includes(search.toLowerCase()),
  )

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (product: Product) => {
    setEditing(product)
    setForm({
      code: product.code ?? '',
      name: product.name,
      description: product.description ?? '',
      type: product.type,
      price: product.price,
      cost: product.cost ?? 0,
      stockQty: product.stockQty ?? 0,
      unit: product.unit ?? '',
      active: product.active,
    })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name.trim()) {
      setFormError(t('products.nameRequired'))
      return
    }
    if (form.price == null || isNaN(Number(form.price))) {
      setFormError(t('products.priceRequired'))
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await update(editing.id, form)
        notify('success', t('products.updated'))
      } else {
        await create(form)
        notify('success', t('products.created'))
      }
      setModalOpen(false)
    } catch (err) {
      setFormError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await remove(deleteTarget.id)
      notify('success', t('products.deleted', { name: deleteTarget.name }))
    } catch (err) {
      notify('error', getErrorMessage(err))
    } finally {
      setDeleting(false)
      setDeleteTarget(null)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.title}>{t('products.title')}</h2>
        <div className={styles.toolbar}>
          <input
            className={styles.searchInput}
            type="search"
            placeholder={t('products.searchPlaceholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button className={styles.addBtn} onClick={openCreate}>
            {t('products.newProduct')}
          </button>
        </div>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button
            className={styles.notificationClose}
            onClick={clearNotification}
            aria-label={t('common.closeNotification')}
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className={styles.error} role="alert">
          {error}
          <button onClick={clearError} style={{ marginLeft: '1rem', cursor: 'pointer' }}>
            ✕
          </button>
        </div>
      )}

      {loading ? (
        <div className={styles.loading}>
          <span className={styles.spinner} aria-hidden="true" />
          {t('common.loading')}
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>{t('products.columns.code')}</th>
                <th>{t('products.columns.name')}</th>
                <th>{t('products.columns.type')}</th>
                <th>{t('products.columns.price')}</th>
                <th>{t('products.columns.stock')}</th>
                <th>{t('products.columns.status')}</th>
                <th>{t('products.columns.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={7} className={styles.emptyState}>
                    {t('products.notFound')}
                  </td>
                </tr>
              ) : (
                filtered.map((product) => (
                  <tr key={product.id}>
                    <td>{product.code || '—'}</td>
                    <td>{product.name}</td>
                    <td>{t(`products.types.${product.type}`)}</td>
                    <td>{formatCurrency(product.price)}</td>
                    <td>
                      {product.type === 'SERVICE'
                        ? '—'
                        : `${product.stockQty ?? 0} ${product.unit || ''}`}
                    </td>
                    <td>
                      <span
                        className={`${styles.badge} ${product.active ? styles.badgeActive : styles.badgeInactive}`}
                      >
                        {product.active ? t('common.active') : t('common.inactive')}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button
                          className={styles.editBtn}
                          onClick={() => openEdit(product)}
                        >
                          {t('common.edit')}
                        </button>
                        <button
                          className={styles.deleteBtn}
                          onClick={() => setDeleteTarget(product)}
                        >
                          {t('common.delete')}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {modalOpen && (
        <Modal
          title={editing ? t('products.editTitle') : t('products.createTitle')}
          onClose={() => setModalOpen(false)}
        >
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={styles.formGroup}>
                <label htmlFor="product-code">{t('products.fields.code')}</label>
                <input
                  id="product-code"
                  type="text"
                  maxLength={50}
                  value={form.code}
                  onChange={(e) => setForm({ ...form, code: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="product-type">{t('products.fields.type')}</label>
                <select
                  id="product-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as ProductType })}
                >
                  <option value="PRODUCT">{t('products.types.PRODUCT')}</option>
                  <option value="SERVICE">{t('products.types.SERVICE')}</option>
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="product-name">{t('products.fields.name')}</label>
                <input
                  id="product-name"
                  type="text"
                  maxLength={150}
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="product-desc">{t('products.fields.description')}</label>
                <textarea
                  id="product-desc"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="product-price">{t('products.fields.price')}</label>
                <input
                  id="product-price"
                  type="number"
                  min="0"
                  step="0.01"
                  required
                  value={form.price}
                  onChange={(e) => setForm({ ...form, price: parseFloat(e.target.value) || 0 })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="product-cost">{t('products.fields.cost')}</label>
                <input
                  id="product-cost"
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.cost}
                  onChange={(e) => setForm({ ...form, cost: parseFloat(e.target.value) || 0 })}
                />
              </div>
              {form.type === 'PRODUCT' && (
                <>
                  <div className={styles.formGroup}>
                    <label htmlFor="product-stock">{t('products.fields.stockQty')}</label>
                    <input
                      id="product-stock"
                      type="number"
                      min="0"
                      value={form.stockQty}
                      onChange={(e) =>
                        setForm({ ...form, stockQty: parseInt(e.target.value) || 0 })
                      }
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label htmlFor="product-unit">{t('products.fields.unit')}</label>
                    <input
                      id="product-unit"
                      type="text"
                      maxLength={20}
                      value={form.unit}
                      onChange={(e) => setForm({ ...form, unit: e.target.value })}
                    />
                  </div>
                </>
              )}
              <div className={styles.formGroup}>
                <label>
                  <input
                    type="checkbox"
                    checked={form.active}
                    onChange={(e) => setForm({ ...form, active: e.target.checked })}
                    style={{ marginRight: '0.5rem' }}
                  />
                  {t('products.fields.active')}
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button
                type="button"
                className={styles.cancelBtn}
                onClick={() => setModalOpen(false)}
              >
                {t('common.cancel')}
              </button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? t('common.saving') : t('common.save')}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={t('products.deleteConfirm', { name: deleteTarget.name })}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  )
}
