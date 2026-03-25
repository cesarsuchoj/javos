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
