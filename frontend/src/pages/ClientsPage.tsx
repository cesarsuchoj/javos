import { useEffect, useState } from 'react'
import { useClientStore } from '../store/clientStore'
import { Client, ClientRequest } from '../types'
import { getErrorMessage } from '../services/api'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import styles from './crud.module.css'

const emptyForm: ClientRequest = {
  name: '',
  email: '',
  phone: '',
  document: '',
  address: '',
  city: '',
  state: '',
  zipCode: '',
  active: true,
  notes: '',
}

export default function ClientsPage() {
  const { clients, loading, error, fetchAll, create, update, remove, clearError } =
    useClientStore()

  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Client | null>(null)
  const [form, setForm] = useState<ClientRequest>(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<Client | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchAll()
  }, [fetchAll])

  const filtered = clients.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      (c.email ?? '').toLowerCase().includes(search.toLowerCase()) ||
      (c.document ?? '').includes(search),
  )

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (client: Client) => {
    setEditing(client)
    setForm({
      name: client.name,
      email: client.email ?? '',
      phone: client.phone ?? '',
      document: client.document ?? '',
      address: client.address ?? '',
      city: client.city ?? '',
      state: client.state ?? '',
      zipCode: client.zipCode ?? '',
      active: client.active,
      notes: client.notes ?? '',
    })
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
        await update(editing.id, form)
      } else {
        await create(form)
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
    } catch (err) {
      alert(getErrorMessage(err))
    } finally {
      setDeleting(false)
      setDeleteTarget(null)
    }
  }

  const field = (
    key: keyof ClientRequest,
    label: string,
    type = 'text',
    maxLength?: number,
  ) => (
    <div className={styles.formGroup}>
      <label htmlFor={`client-${key}`}>{label}</label>
      <input
        id={`client-${key}`}
        type={type}
        maxLength={maxLength}
        value={form[key] as string}
        onChange={(e) => setForm({ ...form, [key]: e.target.value })}
      />
    </div>
  )

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.title}>Clientes</h2>
        <div className={styles.toolbar}>
          <input
            className={styles.searchInput}
            type="search"
            placeholder="Buscar por nome, e-mail ou documento…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button className={styles.addBtn} onClick={openCreate}>
            + Novo Cliente
          </button>
        </div>
      </div>

      {error && (
        <div className={styles.error} role="alert">
          {error}
          <button onClick={clearError} style={{ marginLeft: '1rem', cursor: 'pointer' }}>
            ✕
          </button>
        </div>
      )}

      {loading ? (
        <div className={styles.loading}>Carregando...</div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Telefone</th>
                <th>Documento</th>
                <th>Cidade / UF</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={7} className={styles.emptyState}>
                    Nenhum cliente encontrado.
                  </td>
                </tr>
              ) : (
                filtered.map((client) => (
                  <tr key={client.id}>
                    <td>{client.name}</td>
                    <td>{client.email || '—'}</td>
                    <td>{client.phone || '—'}</td>
                    <td>{client.document || '—'}</td>
                    <td>
                      {[client.city, client.state].filter(Boolean).join(' / ') || '—'}
                    </td>
                    <td>
                      <span
                        className={`${styles.badge} ${client.active ? styles.badgeActive : styles.badgeInactive}`}
                      >
                        {client.active ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button
                          className={styles.editBtn}
                          onClick={() => openEdit(client)}
                        >
                          Editar
                        </button>
                        <button
                          className={styles.deleteBtn}
                          onClick={() => setDeleteTarget(client)}
                        >
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
        <Modal
          title={editing ? 'Editar Cliente' : 'Novo Cliente'}
          onClose={() => setModalOpen(false)}
        >
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="client-name">Nome *</label>
                <input
                  id="client-name"
                  type="text"
                  maxLength={150}
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </div>
              {field('email', 'E-mail', 'email', 100)}
              {field('phone', 'Telefone', 'tel', 20)}
              {field('document', 'CPF / CNPJ', 'text', 20)}
              {field('zipCode', 'CEP', 'text', 10)}
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="client-address">Endereço</label>
                <input
                  id="client-address"
                  type="text"
                  maxLength={255}
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                />
              </div>
              {field('city', 'Cidade', 'text', 100)}
              <div className={styles.formGroup}>
                <label htmlFor="client-state">UF</label>
                <input
                  id="client-state"
                  type="text"
                  maxLength={2}
                  value={form.state}
                  onChange={(e) =>
                    setForm({ ...form, state: e.target.value.toUpperCase() })
                  }
                />
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="client-notes">Observações</label>
                <textarea
                  id="client-notes"
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
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
                  Ativo
                </label>
              </div>
            </div>
            <div className={styles.formActions}>
              <button
                type="button"
                className={styles.cancelBtn}
                onClick={() => setModalOpen(false)}
              >
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
          message={`Deseja excluir o cliente "${deleteTarget.name}"? Esta ação não pode ser desfeita.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  )
}
