import api from './api'
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../types'

export const authService = {
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', request)
    return response.data
  },

  register: async (request: RegisterRequest): Promise<User> => {
    const response = await api.post<User>('/auth/register', request)
    return response.data
  },
}
