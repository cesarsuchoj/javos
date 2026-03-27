import api from './api'
import {
  Charge,
  ChargeRequest,
  ChargeStatus,
  Account,
  AccountRequest,
  Category,
  CategoryRequest,
  FinancialEntry,
  FinancialEntryRequest,
} from '../types'

// ── Charges ──────────────────────────────────────────────────
export const chargeService = {
  getAll: (): Promise<Charge[]> =>
    api.get<Charge[]>('/v1/charges').then((r) => r.data),

  getById: (id: number): Promise<Charge> =>
    api.get<Charge>(`/v1/charges/${id}`).then((r) => r.data),

  create: (data: ChargeRequest): Promise<Charge> =>
    api.post<Charge>('/v1/charges', data).then((r) => r.data),

  update: (id: number, data: ChargeRequest): Promise<Charge> =>
    api.put<Charge>(`/v1/charges/${id}`, data).then((r) => r.data),

  updateStatus: (id: number, status: ChargeStatus): Promise<Charge> =>
    api.patch<Charge>(`/v1/charges/${id}/status`, null, { params: { status } }).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/charges/${id}`).then(() => undefined),
}

// ── Accounts ─────────────────────────────────────────────────
export const accountService = {
  getAll: (): Promise<Account[]> =>
    api.get<Account[]>('/v1/financial/accounts').then((r) => r.data),

  getById: (id: number): Promise<Account> =>
    api.get<Account>(`/v1/financial/accounts/${id}`).then((r) => r.data),

  create: (data: AccountRequest): Promise<Account> =>
    api.post<Account>('/v1/financial/accounts', data).then((r) => r.data),

  update: (id: number, data: AccountRequest): Promise<Account> =>
    api.put<Account>(`/v1/financial/accounts/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/financial/accounts/${id}`).then(() => undefined),
}

// ── Categories ────────────────────────────────────────────────
export const categoryService = {
  getAll: (): Promise<Category[]> =>
    api.get<Category[]>('/v1/financial/categories').then((r) => r.data),

  getById: (id: number): Promise<Category> =>
    api.get<Category>(`/v1/financial/categories/${id}`).then((r) => r.data),

  create: (data: CategoryRequest): Promise<Category> =>
    api.post<Category>('/v1/financial/categories', data).then((r) => r.data),

  update: (id: number, data: CategoryRequest): Promise<Category> =>
    api.put<Category>(`/v1/financial/categories/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/financial/categories/${id}`).then(() => undefined),
}

// ── Financial Entries ─────────────────────────────────────────
export const entryService = {
  getAll: (): Promise<FinancialEntry[]> =>
    api.get<FinancialEntry[]>('/v1/financial/entries').then((r) => r.data),

  getById: (id: number): Promise<FinancialEntry> =>
    api.get<FinancialEntry>(`/v1/financial/entries/${id}`).then((r) => r.data),

  create: (data: FinancialEntryRequest): Promise<FinancialEntry> =>
    api.post<FinancialEntry>('/v1/financial/entries', data).then((r) => r.data),

  update: (id: number, data: FinancialEntryRequest): Promise<FinancialEntry> =>
    api.put<FinancialEntry>(`/v1/financial/entries/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/financial/entries/${id}`).then(() => undefined),
}
