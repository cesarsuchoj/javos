import api from './api'
import { Product, ProductRequest } from '../types'

export const productService = {
  getAll: (name?: string): Promise<Product[]> =>
    api.get<Product[]>('/v1/products', { params: name ? { name } : undefined }).then((r) => r.data),

  getById: (id: number): Promise<Product> =>
    api.get<Product>(`/v1/products/${id}`).then((r) => r.data),

  create: (data: ProductRequest): Promise<Product> =>
    api.post<Product>('/v1/products', data).then((r) => r.data),

  update: (id: number, data: ProductRequest): Promise<Product> =>
    api.put<Product>(`/v1/products/${id}`, data).then((r) => r.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/v1/products/${id}`).then(() => undefined),
}
