import { create } from 'zustand'
import { ServiceOrder, ServiceOrderRequest } from '../types'
import { serviceOrderService } from '../services/serviceOrderService'
import { getErrorMessage } from '../services/api'

interface ServiceOrderState {
  serviceOrders: ServiceOrder[]
  loading: boolean
  error: string | null
  fetchAll: () => Promise<void>
  create: (data: ServiceOrderRequest) => Promise<void>
  update: (id: number, data: ServiceOrderRequest) => Promise<void>
  remove: (id: number) => Promise<void>
  clearError: () => void
}

export const useServiceOrderStore = create<ServiceOrderState>((set) => ({
  serviceOrders: [],
  loading: false,
  error: null,

  fetchAll: async () => {
    set({ loading: true, error: null })
    try {
      const serviceOrders = await serviceOrderService.getAll()
      set({ serviceOrders })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },

  create: async (data: ServiceOrderRequest) => {
    const order = await serviceOrderService.create(data)
    set((s) => ({ serviceOrders: [order, ...s.serviceOrders] }))
  },

  update: async (id: number, data: ServiceOrderRequest) => {
    const updated = await serviceOrderService.update(id, data)
    set((s) => ({
      serviceOrders: s.serviceOrders.map((o) => (o.id === id ? updated : o)),
    }))
  },

  remove: async (id: number) => {
    await serviceOrderService.delete(id)
    set((s) => ({ serviceOrders: s.serviceOrders.filter((o) => o.id !== id) }))
  },

  clearError: () => set({ error: null }),
}))
