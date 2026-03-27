export interface User {
  id: number
  username: string
  email: string
  name: string
  role: string
  active: boolean
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  type: string
  username: string
  name: string
  role: string
}

export interface RegisterRequest {
  username: string
  email: string
  name: string
  password: string
}

export interface DashboardSummary {
  totalUsers: number
  loggedUser: string
  version: string
}

export interface ApiError {
  status: number
  message: string
  timestamp: string
  details?: Record<string, string>
}

// ── Clients ──────────────────────────────────────────────────
export interface Client {
  id: number
  name: string
  email: string
  phone: string
  document: string
  address: string
  city: string
  state: string
  zipCode: string
  active: boolean
  notes: string
  createdAt: string
  updatedAt: string
}

export interface ClientRequest {
  name: string
  email?: string
  phone?: string
  document?: string
  address?: string
  city?: string
  state?: string
  zipCode?: string
  active: boolean
  notes?: string
}

// ── Products ─────────────────────────────────────────────────
export type ProductType = 'PRODUCT' | 'SERVICE'

export interface Product {
  id: number
  code: string
  name: string
  description: string
  type: ProductType
  price: number
  cost: number
  stockQty: number
  unit: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface ProductRequest {
  code?: string
  name: string
  description?: string
  type: ProductType
  price: number
  cost?: number
  stockQty?: number
  unit?: string
  active: boolean
}

// ── Service Orders ────────────────────────────────────────────
export type ServiceOrderStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'WAITING_PARTS'
  | 'DONE'
  | 'CLOSED'
  | 'CANCELLED'

export type ServiceOrderPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'

export interface ServiceOrder {
  id: number
  orderNumber: string
  clientId: number
  clientName: string
  technicianId: number | null
  technicianName: string | null
  status: ServiceOrderStatus
  priority: ServiceOrderPriority
  description: string
  diagnosis: string
  solution: string
  laborCost: number
  estimatedCompletion: string | null
  completedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface ServiceOrderRequest {
  clientId: number
  technicianId?: number | null
  status?: ServiceOrderStatus
  priority?: ServiceOrderPriority
  description: string
  diagnosis?: string
  solution?: string
  laborCost?: number
  estimatedCompletion?: string | null
}

// ── Charges ───────────────────────────────────────────────────
export type ChargeStatus = 'PENDING' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED'
export type ChargeMethod = 'BOLETO' | 'PIX' | 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CASH'

export interface Charge {
  id: number
  clientId: number | null
  clientName: string | null
  referenceId: number | null
  referenceType: string | null
  amount: number
  dueDate: string | null
  status: ChargeStatus
  method: ChargeMethod | null
  externalId: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface ChargeRequest {
  clientId?: number | null
  referenceId?: number | null
  referenceType?: string
  amount: number
  dueDate?: string | null
  status?: ChargeStatus
  method?: ChargeMethod | null
  externalId?: string
  notes?: string
}

// ── Financial – Accounts ──────────────────────────────────────
export type AccountType = 'CHECKING' | 'SAVINGS' | 'CASH' | 'CREDIT_CARD'

export interface Account {
  id: number
  name: string
  type: AccountType
  balance: number
  active: boolean
}

export interface AccountRequest {
  name: string
  type: AccountType
  balance?: number
  active: boolean
}

// ── Financial – Categories ────────────────────────────────────
export type CategoryType = 'INCOME' | 'EXPENSE'

export interface Category {
  id: number
  name: string
  type: CategoryType
  description: string
  active: boolean
}

export interface CategoryRequest {
  name: string
  type: CategoryType
  description?: string
  active: boolean
}

// ── Financial – Entries ───────────────────────────────────────
export type EntryType = 'INCOME' | 'EXPENSE'

export interface FinancialEntry {
  id: number
  description: string
  type: EntryType
  amount: number
  dueDate: string | null
  paymentDate: string | null
  paid: boolean
  categoryId: number | null
  categoryName: string | null
  accountId: number | null
  accountName: string | null
  referenceId: number | null
  referenceType: string | null
  notes: string | null
  createdAt: string
}

export interface FinancialEntryRequest {
  description: string
  type: EntryType
  amount: number
  dueDate?: string | null
  paymentDate?: string | null
  paid?: boolean
  categoryId?: number | null
  accountId?: number | null
  referenceId?: number | null
  referenceType?: string
  notes?: string
}
