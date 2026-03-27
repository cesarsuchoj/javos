import { create } from 'zustand'
import { Client, ClientRequest } from '../types'
import { clientService } from '../services/clientService'
import { getErrorMessage } from '../services/api'

interface ClientState {
  clients: Client[]
  loading: boolean
  error: string | null
  fetchAll: (name?: string) => Promise<void>
  create: (data: ClientRequest) => Promise<void>
  update: (id: number, data: ClientRequest) => Promise<void>
  remove: (id: number) => Promise<void>
  clearError: () => void
}

export const useClientStore = create<ClientState>((set) => ({
  clients: [],
  loading: false,
  error: null,

  fetchAll: async (name?: string) => {
    set({ loading: true, error: null })
    try {
      const clients = await clientService.getAll(name)
      set({ clients })
    } catch (err) {
      set({ error: getErrorMessage(err) })
    } finally {
      set({ loading: false })
    }
  },

  create: async (data: ClientRequest) => {
    const client = await clientService.create(data)
    set((s) => ({ clients: [client, ...s.clients] }))
  },

  update: async (id: number, data: ClientRequest) => {
    const updated = await clientService.update(id, data)
    set((s) => ({ clients: s.clients.map((c) => (c.id === id ? updated : c)) }))
  },

  remove: async (id: number) => {
    await clientService.delete(id)
    set((s) => ({ clients: s.clients.filter((c) => c.id !== id) }))
  },

  clearError: () => set({ error: null }),
}))
