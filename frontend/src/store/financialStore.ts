import { create } from 'zustand'
import {
  Charge,
  ChargeRequest,
  Account,
  AccountRequest,
  Category,
  CategoryRequest,
  FinancialEntry,
  FinancialEntryRequest,
} from '../types'
import {
  chargeService,
  accountService,
  categoryService,
  entryService,
} from '../services/financialService'
import { getErrorMessage } from '../services/api'

interface FinancialState {
  charges: Charge[]
  accounts: Account[]
  categories: Category[]
  entries: FinancialEntry[]
  loading: boolean
  error: string | null

  fetchCharges: () => Promise<void>
  createCharge: (data: ChargeRequest) => Promise<void>
  updateCharge: (id: number, data: ChargeRequest) => Promise<void>
  removeCharge: (id: number) => Promise<void>

  fetchAccounts: () => Promise<void>
  createAccount: (data: AccountRequest) => Promise<void>
  updateAccount: (id: number, data: AccountRequest) => Promise<void>
  removeAccount: (id: number) => Promise<void>

  fetchCategories: () => Promise<void>
  createCategory: (data: CategoryRequest) => Promise<void>
  updateCategory: (id: number, data: CategoryRequest) => Promise<void>
  removeCategory: (id: number) => Promise<void>

  fetchEntries: () => Promise<void>
  createEntry: (data: FinancialEntryRequest) => Promise<void>
  updateEntry: (id: number, data: FinancialEntryRequest) => Promise<void>
  removeEntry: (id: number) => Promise<void>

  clearError: () => void
}

export const useFinancialStore = create<FinancialState>((set) => ({
  charges: [],
  accounts: [],
  categories: [],
  entries: [],
  loading: false,
  error: null,

  // Charges
  fetchCharges: async () => {
    set({ loading: true, error: null })
    try {
      set({ charges: await chargeService.getAll() })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },
  createCharge: async (data) => {
    const charge = await chargeService.create(data)
    set((s) => ({ charges: [charge, ...s.charges] }))
  },
  updateCharge: async (id, data) => {
    const updated = await chargeService.update(id, data)
    set((s) => ({ charges: s.charges.map((c) => (c.id === id ? updated : c)) }))
  },
  removeCharge: async (id) => {
    await chargeService.delete(id)
    set((s) => ({ charges: s.charges.filter((c) => c.id !== id) }))
  },

  // Accounts
  fetchAccounts: async () => {
    set({ loading: true, error: null })
    try {
      set({ accounts: await accountService.getAll() })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },
  createAccount: async (data) => {
    const account = await accountService.create(data)
    set((s) => ({ accounts: [account, ...s.accounts] }))
  },
  updateAccount: async (id, data) => {
    const updated = await accountService.update(id, data)
    set((s) => ({ accounts: s.accounts.map((a) => (a.id === id ? updated : a)) }))
  },
  removeAccount: async (id) => {
    await accountService.delete(id)
    set((s) => ({ accounts: s.accounts.filter((a) => a.id !== id) }))
  },

  // Categories
  fetchCategories: async () => {
    set({ loading: true, error: null })
    try {
      set({ categories: await categoryService.getAll() })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },
  createCategory: async (data) => {
    const category = await categoryService.create(data)
    set((s) => ({ categories: [category, ...s.categories] }))
  },
  updateCategory: async (id, data) => {
    const updated = await categoryService.update(id, data)
    set((s) => ({
      categories: s.categories.map((c) => (c.id === id ? updated : c)),
    }))
  },
  removeCategory: async (id) => {
    await categoryService.delete(id)
    set((s) => ({ categories: s.categories.filter((c) => c.id !== id) }))
  },

  // Entries
  fetchEntries: async () => {
    set({ loading: true, error: null })
    try {
      set({ entries: await entryService.getAll() })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },
  createEntry: async (data) => {
    const entry = await entryService.create(data)
    set((s) => ({ entries: [entry, ...s.entries] }))
  },
  updateEntry: async (id, data) => {
    const updated = await entryService.update(id, data)
    set((s) => ({ entries: s.entries.map((e) => (e.id === id ? updated : e)) }))
  },
  removeEntry: async (id) => {
    await entryService.delete(id)
    set((s) => ({ entries: s.entries.filter((e) => e.id !== id) }))
  },

  clearError: () => set({ error: null }),
}))
