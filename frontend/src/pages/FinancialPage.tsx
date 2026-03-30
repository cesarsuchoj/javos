import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { formatCurrency, formatDate } from '../i18n/locale'
import { useFinancialStore } from '../store/financialStore'
import { useClientStore } from '../store/clientStore'
import {
  Charge,
  ChargeRequest,
  ChargeStatus,
  ChargeMethod,
  Account,
  AccountRequest,
  AccountType,
  Category,
  CategoryRequest,
  CategoryType,
  FinancialEntry,
  FinancialEntryRequest,
  EntryType,
} from '../types'
import { getErrorMessage } from '../services/api'
import { useNotification } from '../hooks/useNotification'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import styles from './crud.module.css'

type Tab = 'charges' | 'accounts' | 'categories' | 'entries'

// ── Charges section ───────────────────────────────────────────
const emptyCharge: ChargeRequest = {
  clientId: null,
  amount: 0,
  dueDate: null,
  status: 'PENDING',
  method: null,
  notes: '',
}

function ChargesSection() {
  const { t } = useTranslation()
  const { charges, loading, error, fetchCharges, createCharge, updateCharge, removeCharge, clearError } =
    useFinancialStore()
  const { clients, fetchAll: fetchClients } = useClientStore()

  const { notification, notify, clearNotification } = useNotification()
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Charge | null>(null)
  const [form, setForm] = useState<ChargeRequest>(emptyCharge)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<Charge | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchCharges()
    fetchClients()
  }, [fetchCharges, fetchClients])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyCharge)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (charge: Charge) => {
    setEditing(charge)
    setForm({
      clientId: charge.clientId,
      amount: charge.amount,
      dueDate: charge.dueDate,
      status: charge.status,
      method: charge.method,
      notes: charge.notes ?? '',
    })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.amount || form.amount <= 0) {
      setFormError(t('financial.charges.amountRequired'))
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateCharge(editing.id, form)
        notify('success', t('financial.charges.updated'))
      } else {
        await createCharge(form)
        notify('success', t('financial.charges.created'))
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
      await removeCharge(deleteTarget.id)
      notify('success', t('financial.charges.deleted'))
    } catch (err) {
      notify('error', getErrorMessage(err))
    } finally {
      setDeleting(false)
      setDeleteTarget(null)
    }
  }

  return (
    <>
      <div className={styles.pageHeader} style={{ marginBottom: '1rem' }}>
        <span />
        <button className={styles.addBtn} onClick={openCreate}>
          {t('financial.charges.newCharge')}
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label={t('common.closeNotification')}>✕</button>
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
                <th>{t('financial.charges.columns.client')}</th>
                <th>{t('financial.charges.columns.amount')}</th>
                <th>{t('financial.charges.columns.dueDate')}</th>
                <th>{t('financial.charges.columns.status')}</th>
                <th>{t('financial.charges.columns.method')}</th>
                <th>{t('financial.charges.columns.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {charges.length === 0 ? (
                <tr>
                  <td colSpan={6} className={styles.emptyState}>
                    {t('financial.charges.notFound')}
                  </td>
                </tr>
              ) : (
                charges.map((charge) => (
                  <tr key={charge.id}>
                    <td>{charge.clientName || '—'}</td>
                    <td>{formatCurrency(charge.amount)}</td>
                    <td>{formatDate(charge.dueDate)}</td>
                    <td>
                      <span className={styles.badge} style={{ background: '#f7fafc', color: '#4a5568' }}>
                        {t(`financial.chargeStatus.${charge.status}`)}
                      </span>
                    </td>
                    <td>{charge.method ? t(`financial.chargeMethod.${charge.method}`) : '—'}</td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(charge)}>
                          {t('common.edit')}
                        </button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(charge)}>
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
        <Modal title={editing ? t('financial.charges.editTitle') : t('financial.charges.createTitle')} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="charge-client">{t('financial.charges.fields.client')}</label>
                <select
                  id="charge-client"
                  value={form.clientId ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, clientId: e.target.value ? parseInt(e.target.value) : null })
                  }
                >
                  <option value="">{t('financial.charges.noClient')}</option>
                  {clients.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-amount">{t('financial.charges.fields.amount')}</label>
                <input
                  id="charge-amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-due">{t('financial.charges.fields.dueDate')}</label>
                <input
                  id="charge-due"
                  type="date"
                  value={form.dueDate ?? ''}
                  onChange={(e) => setForm({ ...form, dueDate: e.target.value || null })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-status">{t('financial.charges.fields.status')}</label>
                <select
                  id="charge-status"
                  value={form.status ?? 'PENDING'}
                  onChange={(e) => setForm({ ...form, status: e.target.value as ChargeStatus })}
                >
                  {(['PENDING', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED'] as ChargeStatus[]).map((s) => (
                    <option key={s} value={s}>{t(`financial.chargeStatus.${s}`)}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-method">{t('financial.charges.fields.method')}</label>
                <select
                  id="charge-method"
                  value={form.method ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, method: (e.target.value as ChargeMethod) || null })
                  }
                >
                  <option value="">{t('financial.charges.noMethod')}</option>
                  {(['BOLETO', 'PIX', 'CREDIT_CARD', 'BANK_TRANSFER', 'CASH'] as ChargeMethod[]).map((m) => (
                    <option key={m} value={m}>{t(`financial.chargeMethod.${m}`)}</option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="charge-notes">{t('financial.charges.fields.notes')}</label>
                <textarea
                  id="charge-notes"
                  value={form.notes ?? ''}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>
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
          message={t('financial.charges.deleteConfirm', { amount: formatCurrency(deleteTarget.amount) })}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </>
  )
}

// ── Accounts section ──────────────────────────────────────────
const emptyAccount: AccountRequest = { name: '', type: 'CHECKING', balance: 0, active: true }

function AccountsSection() {
  const { t } = useTranslation()
  const { accounts, loading, error, fetchAccounts, createAccount, updateAccount, removeAccount, clearError } =
    useFinancialStore()

  const { notification, notify, clearNotification } = useNotification()
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Account | null>(null)
  const [form, setForm] = useState<AccountRequest>(emptyAccount)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<Account | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchAccounts()
  }, [fetchAccounts])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyAccount)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (account: Account) => {
    setEditing(account)
    setForm({ name: account.name, type: account.type, balance: account.balance, active: account.active })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name.trim()) {
      setFormError(t('financial.accounts.nameRequired'))
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateAccount(editing.id, form)
        notify('success', t('financial.accounts.updated'))
      } else {
        await createAccount(form)
        notify('success', t('financial.accounts.created'))
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
      await removeAccount(deleteTarget.id)
      notify('success', t('financial.accounts.deleted', { name: deleteTarget.name }))
    } catch (err) {
      notify('error', getErrorMessage(err))
    } finally {
      setDeleting(false)
      setDeleteTarget(null)
    }
  }

  return (
    <>
      <div className={styles.pageHeader} style={{ marginBottom: '1rem' }}>
        <span />
        <button className={styles.addBtn} onClick={openCreate}>
          {t('financial.accounts.newAccount')}
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label={t('common.closeNotification')}>✕</button>
        </div>
      )}

      {error && (
        <div className={styles.error} role="alert">
          {error}
          <button onClick={clearError} style={{ marginLeft: '1rem', cursor: 'pointer' }}>✕</button>
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
                <th>{t('financial.accounts.columns.name')}</th>
                <th>{t('financial.accounts.columns.type')}</th>
                <th>{t('financial.accounts.columns.balance')}</th>
                <th>{t('financial.accounts.columns.status')}</th>
                <th>{t('financial.accounts.columns.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {accounts.length === 0 ? (
                <tr>
                  <td colSpan={5} className={styles.emptyState}>{t('financial.accounts.notFound')}</td>
                </tr>
              ) : (
                accounts.map((account) => (
                  <tr key={account.id}>
                    <td>{account.name}</td>
                    <td>{t(`financial.accountType.${account.type}`)}</td>
                    <td>{formatCurrency(account.balance)}</td>
                    <td>
                      <span className={`${styles.badge} ${account.active ? styles.badgeActive : styles.badgeInactive}`}>
                        {account.active ? t('common.active_f') : t('common.inactive_f')}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(account)}>{t('common.edit')}</button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(account)}>{t('common.delete')}</button>
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
        <Modal title={editing ? t('financial.accounts.editTitle') : t('financial.accounts.createTitle')} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="acc-name">{t('financial.accounts.fields.name')}</label>
                <input
                  id="acc-name"
                  type="text"
                  maxLength={100}
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="acc-type">{t('financial.accounts.fields.type')}</label>
                <select
                  id="acc-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as AccountType })}
                >
                  {(['CHECKING', 'SAVINGS', 'CASH', 'CREDIT_CARD'] as AccountType[]).map((accountType) => (
                    <option key={accountType} value={accountType}>{t(`financial.accountType.${accountType}`)}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="acc-balance">{t('financial.accounts.fields.balance')}</label>
                <input
                  id="acc-balance"
                  type="number"
                  step="0.01"
                  value={form.balance ?? 0}
                  onChange={(e) => setForm({ ...form, balance: parseFloat(e.target.value) || 0 })}
                />
              </div>
              <div className={styles.formGroup}>
                <label>
                  <input
                    type="checkbox"
                    checked={form.active}
                    onChange={(e) => setForm({ ...form, active: e.target.checked })}
                    style={{ marginRight: '0.5rem' }}
                  />
                  {t('financial.accounts.fields.active')}
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>{t('common.cancel')}</button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? t('common.saving') : t('common.save')}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={t('financial.accounts.deleteConfirm', { name: deleteTarget.name })}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </>
  )
}

// ── Categories section ────────────────────────────────────────
const emptyCategory: CategoryRequest = { name: '', type: 'EXPENSE', description: '', active: true }

function CategoriesSection() {
  const { t } = useTranslation()
  const { categories, loading, error, fetchCategories, createCategory, updateCategory, removeCategory, clearError } =
    useFinancialStore()

  const { notification, notify, clearNotification } = useNotification()
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Category | null>(null)
  const [form, setForm] = useState<CategoryRequest>(emptyCategory)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<Category | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchCategories()
  }, [fetchCategories])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyCategory)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (cat: Category) => {
    setEditing(cat)
    setForm({ name: cat.name, type: cat.type, description: cat.description ?? '', active: cat.active })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name.trim()) {
      setFormError(t('financial.categories.nameRequired'))
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateCategory(editing.id, form)
        notify('success', t('financial.categories.updated'))
      } else {
        await createCategory(form)
        notify('success', t('financial.categories.created'))
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
      await removeCategory(deleteTarget.id)
      notify('success', t('financial.categories.deleted', { name: deleteTarget.name }))
    } catch (err) {
      notify('error', getErrorMessage(err))
    } finally {
      setDeleting(false)
      setDeleteTarget(null)
    }
  }

  return (
    <>
      <div className={styles.pageHeader} style={{ marginBottom: '1rem' }}>
        <span />
        <button className={styles.addBtn} onClick={openCreate}>
          {t('financial.categories.newCategory')}
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label={t('common.closeNotification')}>✕</button>
        </div>
      )}

      {error && (
        <div className={styles.error} role="alert">
          {error}
          <button onClick={clearError} style={{ marginLeft: '1rem', cursor: 'pointer' }}>✕</button>
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
                <th>{t('financial.categories.columns.name')}</th>
                <th>{t('financial.categories.columns.type')}</th>
                <th>{t('financial.categories.columns.description')}</th>
                <th>{t('financial.categories.columns.status')}</th>
                <th>{t('financial.categories.columns.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {categories.length === 0 ? (
                <tr>
                  <td colSpan={5} className={styles.emptyState}>{t('financial.categories.notFound')}</td>
                </tr>
              ) : (
                categories.map((cat) => (
                  <tr key={cat.id}>
                    <td>{cat.name}</td>
                    <td>{t(`financial.entryType.${cat.type}`)}</td>
                    <td>{cat.description || '—'}</td>
                    <td>
                      <span className={`${styles.badge} ${cat.active ? styles.badgeActive : styles.badgeInactive}`}>
                        {cat.active ? t('common.active_f') : t('common.inactive_f')}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(cat)}>{t('common.edit')}</button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(cat)}>{t('common.delete')}</button>
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
        <Modal title={editing ? t('financial.categories.editTitle') : t('financial.categories.createTitle')} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="cat-name">{t('financial.categories.fields.name')}</label>
                <input
                  id="cat-name"
                  type="text"
                  maxLength={100}
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="cat-type">{t('financial.categories.fields.type')}</label>
                <select
                  id="cat-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as CategoryType })}
                >
                  {(['INCOME', 'EXPENSE'] as CategoryType[]).map((catType) => (
                    <option key={catType} value={catType}>{t(`financial.entryType.${catType}`)}</option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="cat-desc">{t('financial.categories.fields.description')}</label>
                <textarea
                  id="cat-desc"
                  value={form.description ?? ''}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label>
                  <input
                    type="checkbox"
                    checked={form.active}
                    onChange={(e) => setForm({ ...form, active: e.target.checked })}
                    style={{ marginRight: '0.5rem' }}
                  />
                  {t('financial.categories.fields.active')}
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>{t('common.cancel')}</button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? t('common.saving') : t('common.save')}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={t('financial.categories.deleteConfirm', { name: deleteTarget.name })}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </>
  )
}

// ── Entries section ───────────────────────────────────────────
const emptyEntry: FinancialEntryRequest = {
  description: '',
  type: 'EXPENSE',
  amount: 0,
  dueDate: null,
  paymentDate: null,
  paid: false,
  categoryId: null,
  accountId: null,
  notes: '',
}

function EntriesSection() {
  const { t } = useTranslation()
  const { entries, accounts, categories, loading, error, fetchEntries, fetchAccounts, fetchCategories, createEntry, updateEntry, removeEntry, clearError } =
    useFinancialStore()

  const { notification, notify, clearNotification } = useNotification()
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<FinancialEntry | null>(null)
  const [form, setForm] = useState<FinancialEntryRequest>(emptyEntry)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<FinancialEntry | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchEntries()
    fetchAccounts()
    fetchCategories()
  }, [fetchEntries, fetchAccounts, fetchCategories])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyEntry)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (entry: FinancialEntry) => {
    setEditing(entry)
    setForm({
      description: entry.description,
      type: entry.type,
      amount: entry.amount,
      dueDate: entry.dueDate,
      paymentDate: entry.paymentDate,
      paid: entry.paid,
      categoryId: entry.categoryId,
      accountId: entry.accountId,
      notes: entry.notes ?? '',
    })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.description.trim()) {
      setFormError(t('financial.entries.descriptionRequired'))
      return
    }
    if (!form.amount || form.amount <= 0) {
      setFormError(t('financial.entries.amountRequired'))
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateEntry(editing.id, form)
        notify('success', t('financial.entries.updated'))
      } else {
        await createEntry(form)
        notify('success', t('financial.entries.created'))
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
      await removeEntry(deleteTarget.id)
      notify('success', t('financial.entries.deleted', { description: deleteTarget.description }))
    } catch (err) {
      notify('error', getErrorMessage(err))
    } finally {
      setDeleting(false)
      setDeleteTarget(null)
    }
  }

  return (
    <>
      <div className={styles.pageHeader} style={{ marginBottom: '1rem' }}>
        <span />
        <button className={styles.addBtn} onClick={openCreate}>
          {t('financial.entries.newEntry')}
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label={t('common.closeNotification')}>✕</button>
        </div>
      )}

      {error && (
        <div className={styles.error} role="alert">
          {error}
          <button onClick={clearError} style={{ marginLeft: '1rem', cursor: 'pointer' }}>✕</button>
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
                <th>{t('financial.entries.columns.description')}</th>
                <th>{t('financial.entries.columns.type')}</th>
                <th>{t('financial.entries.columns.amount')}</th>
                <th>{t('financial.entries.columns.dueDate')}</th>
                <th>{t('financial.entries.columns.paid')}</th>
                <th>{t('financial.entries.columns.category')}</th>
                <th>{t('financial.entries.columns.account')}</th>
                <th>{t('financial.entries.columns.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {entries.length === 0 ? (
                <tr>
                  <td colSpan={8} className={styles.emptyState}>{t('financial.entries.notFound')}</td>
                </tr>
              ) : (
                entries.map((entry) => (
                  <tr key={entry.id}>
                    <td>{entry.description}</td>
                    <td>
                      <span
                        className={styles.badge}
                        style={{
                          background: entry.type === 'INCOME' ? '#f0fff4' : '#fff5f5',
                          color: entry.type === 'INCOME' ? '#276749' : '#c53030',
                        }}
                      >
                        {t(`financial.entryType.${entry.type}`)}
                      </span>
                    </td>
                    <td>{formatCurrency(entry.amount)}</td>
                    <td>{formatDate(entry.dueDate)}</td>
                    <td>
                      <span className={`${styles.badge} ${entry.paid ? styles.badgeActive : styles.badgeInactive}`}>
                        {entry.paid ? t('common.yes') : t('common.no')}
                      </span>
                    </td>
                    <td>{entry.categoryName || '—'}</td>
                    <td>{entry.accountName || '—'}</td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(entry)}>{t('common.edit')}</button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(entry)}>{t('common.delete')}</button>
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
        <Modal title={editing ? t('financial.entries.editTitle') : t('financial.entries.createTitle')} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="entry-desc">{t('financial.entries.fields.description')}</label>
                <input
                  id="entry-desc"
                  type="text"
                  required
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-type">{t('financial.entries.fields.type')}</label>
                <select
                  id="entry-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as EntryType })}
                >
                  <option value="INCOME">{t('financial.entryType.INCOME')}</option>
                  <option value="EXPENSE">{t('financial.entryType.EXPENSE')}</option>
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-amount">{t('financial.entries.fields.amount')}</label>
                <input
                  id="entry-amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  required
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-due">{t('financial.entries.fields.dueDate')}</label>
                <input
                  id="entry-due"
                  type="date"
                  value={form.dueDate ?? ''}
                  onChange={(e) => setForm({ ...form, dueDate: e.target.value || null })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-payment">{t('financial.entries.fields.paymentDate')}</label>
                <input
                  id="entry-payment"
                  type="date"
                  value={form.paymentDate ?? ''}
                  onChange={(e) => setForm({ ...form, paymentDate: e.target.value || null })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-category">{t('financial.entries.fields.category')}</label>
                <select
                  id="entry-category"
                  value={form.categoryId ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, categoryId: e.target.value ? parseInt(e.target.value) : null })
                  }
                >
                  <option value="">{t('financial.entries.fields.noCategory')}</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-account">{t('financial.entries.fields.account')}</label>
                <select
                  id="entry-account"
                  value={form.accountId ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, accountId: e.target.value ? parseInt(e.target.value) : null })
                  }
                >
                  <option value="">{t('financial.entries.fields.noAccount')}</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>{a.name}</option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="entry-notes">{t('financial.entries.fields.notes')}</label>
                <textarea
                  id="entry-notes"
                  value={form.notes ?? ''}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label>
                  <input
                    type="checkbox"
                    checked={form.paid ?? false}
                    onChange={(e) => setForm({ ...form, paid: e.target.checked })}
                    style={{ marginRight: '0.5rem' }}
                  />
                  {t('financial.entries.fields.paid')}
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>{t('common.cancel')}</button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? t('common.saving') : t('common.save')}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={t('financial.entries.deleteConfirm', { description: deleteTarget.description })}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </>
  )
}

// ── Main FinancialPage ────────────────────────────────────────
export default function FinancialPage() {
  const { t } = useTranslation()
  const [activeTab, setActiveTab] = useState<Tab>('charges')

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.title}>{t('financial.title')}</h2>
      </div>

      <div className={styles.tabs}>
        {(
          [
            { key: 'charges', label: t('financial.tabs.charges') },
            { key: 'accounts', label: t('financial.tabs.accounts') },
            { key: 'categories', label: t('financial.tabs.categories') },
            { key: 'entries', label: t('financial.tabs.entries') },
          ] as { key: Tab; label: string }[]
        ).map(({ key, label }) => (
          <button
            key={key}
            className={`${styles.tab} ${activeTab === key ? styles.tabActive : ''}`}
            onClick={() => setActiveTab(key)}
          >
            {label}
          </button>
        ))}
      </div>

      {activeTab === 'charges' && <ChargesSection />}
      {activeTab === 'accounts' && <AccountsSection />}
      {activeTab === 'categories' && <CategoriesSection />}
      {activeTab === 'entries' && <EntriesSection />}
    </div>
  )
}
