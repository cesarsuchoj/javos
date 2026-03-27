import { useEffect, useState } from 'react'
import { useServiceOrderStore } from '../store/serviceOrderStore'
import { useClientStore } from '../store/clientStore'
import {
  ServiceOrder,
  ServiceOrderRequest,
  ServiceOrderStatus,
  ServiceOrderPriority,
} from '../types'
import { getErrorMessage } from '../services/api'
import { useNotification } from '../hooks/useNotification'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import styles from './crud.module.css'

const statusLabels: Record<ServiceOrderStatus, string> = {
  OPEN: 'Aberta',
  IN_PROGRESS: 'Em Andamento',
  WAITING_PARTS: 'Aguardando Peças',
  DONE: 'Concluída',
  CLOSED: 'Fechada',
  CANCELLED: 'Cancelada',
}

const priorityLabels: Record<ServiceOrderPriority, string> = {
  LOW: 'Baixa',
  NORMAL: 'Normal',
  HIGH: 'Alta',
  URGENT: 'Urgente',
}

const statusColors: Record<ServiceOrderStatus, string> = {
  OPEN: '#ebf8ff',
  IN_PROGRESS: '#fefcbf',
  WAITING_PARTS: '#fff5f5',
  DONE: '#f0fff4',
  CLOSED: '#f7fafc',
  CANCELLED: '#fff5f5',
}

const statusTextColors: Record<ServiceOrderStatus, string> = {
  OPEN: '#2b6cb0',
  IN_PROGRESS: '#744210',
  WAITING_PARTS: '#c05621',
  DONE: '#276749',
  CLOSED: '#718096',
  CANCELLED: '#c53030',
}

const emptyForm: ServiceOrderRequest = {
  clientId: 0,
  description: '',
  status: 'OPEN',
  priority: 'NORMAL',
  diagnosis: '',
  solution: '',
  laborCost: 0,
  estimatedCompletion: null,
}

export default function ServiceOrdersPage() {
  const {
    serviceOrders,
    loading,
    error,
    fetchAll,
    create,
    update,
    remove,
    clearError,
  } = useServiceOrderStore()
  const { clients, fetchAll: fetchClients } = useClientStore()

  const { notification, notify, clearNotification } = useNotification()
  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<ServiceOrder | null>(null)
  const [form, setForm] = useState<ServiceOrderRequest>(emptyForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<ServiceOrder | null>(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    fetchAll()
    fetchClients()
  }, [fetchAll, fetchClients])

  const filtered = serviceOrders.filter(
    (o) =>
      (o.orderNumber ?? '').toLowerCase().includes(search.toLowerCase()) ||
      (o.clientName ?? '').toLowerCase().includes(search.toLowerCase()) ||
      o.description.toLowerCase().includes(search.toLowerCase()),
  )

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (order: ServiceOrder) => {
    setEditing(order)
    setForm({
      clientId: order.clientId,
      description: order.description,
      status: order.status,
      priority: order.priority,
      diagnosis: order.diagnosis ?? '',
      solution: order.solution ?? '',
      laborCost: order.laborCost ?? 0,
      estimatedCompletion: order.estimatedCompletion ?? null,
    })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.clientId) {
      setFormError('Selecione um cliente.')
      return
    }
    if (!form.description.trim()) {
      setFormError('A descrição é obrigatória.')
      return
    }
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await update(editing.id, form)
        notify('success', 'Ordem de serviço atualizada com sucesso.')
      } else {
        await create(form)
        notify('success', 'Ordem de serviço criada com sucesso.')
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
      notify('success', `OS "${deleteTarget.orderNumber || `#${deleteTarget.id}`}" excluída com sucesso.`)
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
        <h2 className={styles.title}>Ordens de Serviço</h2>
        <div className={styles.toolbar}>
          <input
            className={styles.searchInput}
            type="search"
            placeholder="Buscar por nº, cliente ou descrição…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button className={styles.addBtn} onClick={openCreate}>
            + Nova OS
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
            aria-label="Fechar notificação"
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
          Carregando...
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Nº OS</th>
                <th>Cliente</th>
                <th>Descrição</th>
                <th>Status</th>
                <th>Prioridade</th>
                <th>Criada em</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={7} className={styles.emptyState}>
                    Nenhuma ordem de serviço encontrada.
                  </td>
                </tr>
              ) : (
                filtered.map((order) => (
                  <tr key={order.id}>
                    <td>{order.orderNumber || `#${order.id}`}</td>
                    <td>{order.clientName || '—'}</td>
                    <td
                      style={{
                        maxWidth: '200px',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {order.description}
                    </td>
                    <td>
                      <span
                        className={styles.badge}
                        style={{
                          background: statusColors[order.status],
                          color: statusTextColors[order.status],
                        }}
                      >
                        {statusLabels[order.status]}
                      </span>
                    </td>
                    <td>{priorityLabels[order.priority]}</td>
                    <td>
                      {order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString('pt-BR')
                        : '—'}
                    </td>
                    <td>
                      <div className={styles.actions}>
                        <button
                          className={styles.editBtn}
                          onClick={() => openEdit(order)}
                        >
                          Editar
                        </button>
                        <button
                          className={styles.deleteBtn}
                          onClick={() => setDeleteTarget(order)}
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
          title={editing ? `Editar OS ${editing.orderNumber || `#${editing.id}`}` : 'Nova Ordem de Serviço'}
          onClose={() => setModalOpen(false)}
        >
          <form className={styles.form} onSubmit={handleSubmit}>
            {formError && <div className={styles.formError}>{formError}</div>}
            <div className={styles.formRow}>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="os-client">Cliente *</label>
                <select
                  id="os-client"
                  required
                  value={form.clientId || ''}
                  onChange={(e) =>
                    setForm({ ...form, clientId: parseInt(e.target.value) || 0 })
                  }
                >
                  <option value="">Selecione um cliente…</option>
                  {clients.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="os-status">Status</label>
                <select
                  id="os-status"
                  value={form.status}
                  onChange={(e) =>
                    setForm({ ...form, status: e.target.value as ServiceOrderStatus })
                  }
                >
                  {(Object.keys(statusLabels) as ServiceOrderStatus[]).map((s) => (
                    <option key={s} value={s}>
                      {statusLabels[s]}
                    </option>
                  ))}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="os-priority">Prioridade</label>
                <select
                  id="os-priority"
                  value={form.priority}
                  onChange={(e) =>
                    setForm({ ...form, priority: e.target.value as ServiceOrderPriority })
                  }
                >
                  {(Object.keys(priorityLabels) as ServiceOrderPriority[]).map((p) => (
                    <option key={p} value={p}>
                      {priorityLabels[p]}
                    </option>
                  ))}
                </select>
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="os-description">Descrição *</label>
                <textarea
                  id="os-description"
                  required
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="os-diagnosis">Diagnóstico</label>
                <textarea
                  id="os-diagnosis"
                  value={form.diagnosis}
                  onChange={(e) => setForm({ ...form, diagnosis: e.target.value })}
                />
              </div>
              <div className={`${styles.formGroup} ${styles.fullWidth}`}>
                <label htmlFor="os-solution">Solução</label>
                <textarea
                  id="os-solution"
                  value={form.solution}
                  onChange={(e) => setForm({ ...form, solution: e.target.value })}
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="os-labor">Mão de Obra (R$)</label>
                <input
                  id="os-labor"
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.laborCost}
                  onChange={(e) =>
                    setForm({ ...form, laborCost: parseFloat(e.target.value) || 0 })
                  }
                />
              </div>
              <div className={styles.formGroup}>
                <label htmlFor="os-eta">Previsão de Conclusão</label>
                <input
                  id="os-eta"
                  type="date"
                  value={form.estimatedCompletion ?? ''}
                  onChange={(e) =>
                    setForm({ ...form, estimatedCompletion: e.target.value || null })
                  }
                />
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
          message={`Deseja excluir a OS "${deleteTarget.orderNumber || `#${deleteTarget.id}`}"? Esta ação não pode ser desfeita.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  )
}
