import api from './api'
import { Client, ClientRequest } from '../types'

export const clientService = {
  getAll: (name?: string): Promise<Client[]> =>
    api.get<Client[]>('/v1/clients', { params: name ? { name } : undefined }).then((r) => r.data),

  getById: (id: number): Promise<Client> =>
    api.get<Client>(`/v1/clients/${id}`).then((r) => r.data),

  create: (data: ClientRequest): Promise<Client> =>
    api.post<Client>('/v1/clients', data).then((r) => r.data),

  update: (id: number, data: ClientRequest): Promise<Client> =>
    api.put<Client>(`/v1/clients/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/clients/${id}`).then(() => undefined),
}
