import { useEffect, useState } from 'react'
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

// ── Labels ──────────────────────────────────────────────────
const chargeStatusLabels: Record<ChargeStatus, string> = {
  PENDING: 'Pendente',
  SENT: 'Enviada',
  PAID: 'Paga',
  OVERDUE: 'Vencida',
  CANCELLED: 'Cancelada',
}

const chargeMethodLabels: Record<ChargeMethod, string> = {
  BOLETO: 'Boleto',
  PIX: 'Pix',
  CREDIT_CARD: 'Cartão de Crédito',
  BANK_TRANSFER: 'Transferência',
  CASH: 'Dinheiro',
}

const accountTypeLabels: Record<AccountType, string> = {
  CHECKING: 'Conta Corrente',
  SAVINGS: 'Poupança',
  CASH: 'Caixa',
  CREDIT_CARD: 'Cartão de Crédito',
}

const categoryTypeLabels: Record<CategoryType, string> = {
  INCOME: 'Receita',
  EXPENSE: 'Despesa',
}

const entryTypeLabels: Record<EntryType, string> = {
  INCOME: 'Receita',
  EXPENSE: 'Despesa',
}

const formatCurrency = (v: number | null | undefined) =>
  (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const formatDate = (d: string | null | undefined) =>
  d ? new Date(d).toLocaleDateString('pt-BR') : '—'

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
      setFormError('Informe um valor válido.')
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateCharge(editing.id, form)
        notify('success', 'Cobrança atualizada com sucesso.')
      } else {
        await createCharge(form)
        notify('success', 'Cobrança criada com sucesso.')
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
      notify('success', 'Cobrança excluída com sucesso.')
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
          + Nova Cobrança
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label="Fechar notificação">✕</button>
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
          Carregando...
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Valor</th>
                <th>Vencimento</th>
                <th>Status</th>
                <th>Método</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {charges.length === 0 ? (
                <tr>
                  <td colSpan={6} className={styles.emptyState}>
                    Nenhuma cobrança encontrada.
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
                        {chargeStatusLabels[charge.status]}
                      </span>
                    </td>
                    <td>{charge.method ? chargeMethodLabels[charge.method] : '—'}</td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(charge)}>
                          Editar
                        </button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(charge)}>
                          Excluir
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
        <Modal title={editing ? 'Editar Cobrança' : 'Nova Cobrança'} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="charge-client">Cliente</label>
                <select
                  id="charge-client"
                  value={form.clientId ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, clientId: e.target.value ? parseInt(e.target.value) : null })
                  }
                >
                  <option value="">Sem cliente</option>
                  {clients.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-amount">Valor (R$) *</label>
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
                <label htmlFor="charge-due">Vencimento</label>
                <input
                  id="charge-due"
                  type="date"
                  value={form.dueDate ?? ''}
                  onChange={(e) => setForm({ ...form, dueDate: e.target.value || null })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-status">Status</label>
                <select
                  id="charge-status"
                  value={form.status ?? 'PENDING'}
                  onChange={(e) => setForm({ ...form, status: e.target.value as ChargeStatus })}
                >
                  {(Object.keys(chargeStatusLabels) as ChargeStatus[]).map((s) => (
                    <option key={s} value={s}>{chargeStatusLabels[s]}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="charge-method">Método</label>
                <select
                  id="charge-method"
                  value={form.method ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, method: (e.target.value as ChargeMethod) || null })
                  }
                >
                  <option value="">Não informado</option>
                  {(Object.keys(chargeMethodLabels) as ChargeMethod[]).map((m) => (
                    <option key={m} value={m}>{chargeMethodLabels[m]}</option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="charge-notes">Observações</label>
                <textarea
                  id="charge-notes"
                  value={form.notes ?? ''}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>
                Cancelar
              </button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? 'Salvando...' : 'Salvar'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={`Deseja excluir esta cobrança de ${formatCurrency(deleteTarget.amount)}? Esta ação não pode ser desfeita.`}
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
      setFormError('O nome é obrigatório.')
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateAccount(editing.id, form)
        notify('success', 'Conta atualizada com sucesso.')
      } else {
        await createAccount(form)
        notify('success', 'Conta criada com sucesso.')
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
      notify('success', `Conta "${deleteTarget.name}" excluída com sucesso.`)
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
          + Nova Conta
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label="Fechar notificação">✕</button>
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
          Carregando...
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Tipo</th>
                <th>Saldo</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {accounts.length === 0 ? (
                <tr>
                  <td colSpan={5} className={styles.emptyState}>Nenhuma conta encontrada.</td>
                </tr>
              ) : (
                accounts.map((account) => (
                  <tr key={account.id}>
                    <td>{account.name}</td>
                    <td>{accountTypeLabels[account.type]}</td>
                    <td>{formatCurrency(account.balance)}</td>
                    <td>
                      <span className={`${styles.badge} ${account.active ? styles.badgeActive : styles.badgeInactive}`}>
                        {account.active ? 'Ativa' : 'Inativa'}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(account)}>Editar</button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(account)}>Excluir</button>
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
        <Modal title={editing ? 'Editar Conta' : 'Nova Conta'} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="acc-name">Nome *</label>
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
                <label htmlFor="acc-type">Tipo *</label>
                <select
                  id="acc-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as AccountType })}
                >
                  {(Object.keys(accountTypeLabels) as AccountType[]).map((t) => (
                    <option key={t} value={t}>{accountTypeLabels[t]}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="acc-balance">Saldo (R$)</label>
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
                  Ativa
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>Cancelar</button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? 'Salvando...' : 'Salvar'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={`Deseja excluir a conta "${deleteTarget.name}"? Esta ação não pode ser desfeita.`}
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
      setFormError('O nome é obrigatório.')
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateCategory(editing.id, form)
        notify('success', 'Categoria atualizada com sucesso.')
      } else {
        await createCategory(form)
        notify('success', 'Categoria criada com sucesso.')
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
      notify('success', `Categoria "${deleteTarget.name}" excluída com sucesso.`)
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
          + Nova Categoria
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label="Fechar notificação">✕</button>
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
          Carregando...
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Tipo</th>
                <th>Descrição</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {categories.length === 0 ? (
                <tr>
                  <td colSpan={5} className={styles.emptyState}>Nenhuma categoria encontrada.</td>
                </tr>
              ) : (
                categories.map((cat) => (
                  <tr key={cat.id}>
                    <td>{cat.name}</td>
                    <td>{categoryTypeLabels[cat.type]}</td>
                    <td>{cat.description || '—'}</td>
                    <td>
                      <span className={`${styles.badge} ${cat.active ? styles.badgeActive : styles.badgeInactive}`}>
                        {cat.active ? 'Ativa' : 'Inativa'}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(cat)}>Editar</button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(cat)}>Excluir</button>
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
        <Modal title={editing ? 'Editar Categoria' : 'Nova Categoria'} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="cat-name">Nome *</label>
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
                <label htmlFor="cat-type">Tipo *</label>
                <select
                  id="cat-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as CategoryType })}
                >
                  {(Object.keys(categoryTypeLabels) as CategoryType[]).map((t) => (
                    <option key={t} value={t}>{categoryTypeLabels[t]}</option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="cat-desc">Descrição</label>
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
                  Ativa
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>Cancelar</button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? 'Salvando...' : 'Salvar'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={`Deseja excluir a categoria "${deleteTarget.name}"? Esta ação não pode ser desfeita.`}
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
      setFormError('A descrição é obrigatória.')
      return
    }
    if (!form.amount || form.amount <= 0) {
      setFormError('Informe um valor válido.')
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateEntry(editing.id, form)
        notify('success', 'Lançamento atualizado com sucesso.')
      } else {
        await createEntry(form)
        notify('success', 'Lançamento criado com sucesso.')
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
      notify('success', `Lançamento "${deleteTarget.description}" excluído com sucesso.`)
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
          + Novo Lançamento
        </button>
      </div>

      {notification && (
        <div
          className={`${styles.notification} ${notification.type === 'success' ? styles.notificationSuccess : styles.notificationError}`}
          role={notification.type === 'error' ? 'alert' : 'status'}
        >
          {notification.message}
          <button className={styles.notificationClose} onClick={clearNotification} aria-label="Fechar notificação">✕</button>
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
          Carregando...
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Descrição</th>
                <th>Tipo</th>
                <th>Valor</th>
                <th>Vencimento</th>
                <th>Pago</th>
                <th>Categoria</th>
                <th>Conta</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {entries.length === 0 ? (
                <tr>
                  <td colSpan={8} className={styles.emptyState}>Nenhum lançamento encontrado.</td>
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
                        {entryTypeLabels[entry.type]}
                      </span>
                    </td>
                    <td>{formatCurrency(entry.amount)}</td>
                    <td>{formatDate(entry.dueDate)}</td>
                    <td>
                      <span className={`${styles.badge} ${entry.paid ? styles.badgeActive : styles.badgeInactive}`}>
                        {entry.paid ? 'Sim' : 'Não'}
                      </span>
                    </td>
                    <td>{entry.categoryName || '—'}</td>
                    <td>{entry.accountName || '—'}</td>
                    <td>
                      <div className={styles.actions}>
                        <button className={styles.editBtn} onClick={() => openEdit(entry)}>Editar</button>
                        <button className={styles.deleteBtn} onClick={() => setDeleteTarget(entry)}>Excluir</button>
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
        <Modal title={editing ? 'Editar Lançamento' : 'Novo Lançamento'} onClose={() => setModalOpen(false)}>
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="entry-desc">Descrição *</label>
                <input
                  id="entry-desc"
                  type="text"
                  required
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-type">Tipo *</label>
                <select
                  id="entry-type"
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value as EntryType })}
                >
                  <option value="INCOME">Receita</option>
                  <option value="EXPENSE">Despesa</option>
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-amount">Valor (R$) *</label>
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
                <label htmlFor="entry-due">Vencimento</label>
                <input
                  id="entry-due"
                  type="date"
                  value={form.dueDate ?? ''}
                  onChange={(e) => setForm({ ...form, dueDate: e.target.value || null })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-payment">Data de Pagamento</label>
                <input
                  id="entry-payment"
                  type="date"
                  value={form.paymentDate ?? ''}
                  onChange={(e) => setForm({ ...form, paymentDate: e.target.value || null })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-category">Categoria</label>
                <select
                  id="entry-category"
                  value={form.categoryId ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, categoryId: e.target.value ? parseInt(e.target.value) : null })
                  }
                >
                  <option value="">Sem categoria</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="entry-account">Conta</label>
                <select
                  id="entry-account"
                  value={form.accountId ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, accountId: e.target.value ? parseInt(e.target.value) : null })
                  }
                >
                  <option value="">Sem conta</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>{a.name}</option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="entry-notes">Observações</label>
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
                  Pago
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button type="button" className={styles.cancelBtn} onClick={() => setModalOpen(false)}>Cancelar</button>
              <button type="submit" className={styles.submitBtn} disabled={saving}>
                {saving ? 'Salvando...' : 'Salvar'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleteTarget && (
        <ConfirmDialog
          message={`Deseja excluir o lançamento "${deleteTarget.description}"? Esta ação não pode ser desfeita.`}
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
  const [activeTab, setActiveTab] = useState<Tab>('charges')

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.title}>Financeiro</h2>
      </div>

      <div className={styles.tabs}>
        {(
          [
            { key: 'charges', label: 'Cobranças' },
            { key: 'accounts', label: 'Contas' },
            { key: 'categories', label: 'Categorias' },
            { key: 'entries', label: 'Lançamentos' },
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
