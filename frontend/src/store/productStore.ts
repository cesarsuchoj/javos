import { create } from 'zustand'
import { Product, ProductRequest } from '../types'
import { productService } from '../services/productService'
import { getErrorMessage } from '../services/api'

interface ProductState {
  products: Product[]
  loading: boolean
  error: string | null
  fetchAll: (name?: string) => Promise<void>
  create: (data: ProductRequest) => Promise<void>
  update: (id: number, data: ProductRequest) => Promise<void>
  remove: (id: number) => Promise<void>
  clearError: () => void
}

export const useProductStore = create<ProductState>((set) => ({
  products: [],
  loading: false,
  error: null,

  fetchAll: async (name?: string) => {
    set({ loading: true, error: null })
    try {
      const products = await productService.getAll(name)
      set({ products })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },

  create: async (data: ProductRequest) => {
    const product = await productService.create(data)
    set((s) => ({ products: [product, ...s.products] }))
  },

  update: async (id: number, data: ProductRequest) => {
    const updated = await productService.update(id, data)
    set((s) => ({ products: s.products.map((p) => (p.id === id ? updated : p)) }))
  },

  remove: async (id: number) => {
    await productService.delete(id)
    set((s) => ({ products: s.products.filter((p) => p.id !== id) }))
  },

  clearError: () => set({ error: null }),
}))
