import api from './api'
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../types'

export const authService = {
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/v1/auth/login', request)
    return response.data
  },

  register: async (request: RegisterRequest): Promise<User> => {
    const response = await api.post<User>('/v1/auth/register', request)
    return response.data
  },

  refresh: async (refreshToken: string): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/v1/auth/refresh', { refreshToken })
    return response.data
  },

  logout: async (refreshToken: string): Promise<void> => {
    await api.post('/v1/auth/logout', { refreshToken })
  },
}
