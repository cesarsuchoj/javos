import api from './api'
import { ServiceOrder, ServiceOrderRequest, ServiceOrderStatus } from '../types'

export const serviceOrderService = {
  getAll: (): Promise<ServiceOrder[]> =>
    api.get<ServiceOrder[]>('/v1/service-orders').then((r) => r.data),

  getById: (id: number): Promise<ServiceOrder> =>
    api.get<ServiceOrder>(`/v1/service-orders/${id}`).then((r) => r.data),

  create: (data: ServiceOrderRequest): Promise<ServiceOrder> =>
    api.post<ServiceOrder>('/v1/service-orders', data).then((r) => r.data),

  update: (id: number, data: ServiceOrderRequest): Promise<ServiceOrder> =>
    api.put<ServiceOrder>(`/v1/service-orders/${id}`, data).then((r) => r.data),

  updateStatus: (id: number, status: ServiceOrderStatus): Promise<ServiceOrder> =>
    api
      .patch<ServiceOrder>(`/v1/service-orders/${id}/status`, null, { params: { status } })
      .then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/service-orders/${id}`).then(() => undefined),
}
