import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token!)
    }
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = useAuthStore.getState().refreshToken

      // No refresh token available – go to login
      if (!refreshToken) {
        useAuthStore.getState().logout()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const response = await axios.post<{ token: string }>('/api/v1/auth/refresh', { refreshToken })
        const newToken = response.data.token
        useAuthStore.getState().setToken(newToken)
        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        useAuthStore.getState().logout()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

/**
 * Retorna uma mensagem de erro amigável baseada no tipo de falha da requisição.
 *
 * Casos tratados:
 * - Sem resposta (rede indisponível): mensagem de conexão
 * - 400 Bad Request: dados inválidos
 * - 401 Unauthorized: credenciais incorretas
 * - 403 Forbidden: sem permissão
 * - 404 Not Found: recurso não encontrado
 * - 409 Conflict: registro duplicado
 * - 500 Internal Server Error: erro interno do servidor
 * - 503 Service Unavailable: serviço temporariamente indisponível
 * - Outros: mensagem genérica
 */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Não foi possível conectar ao servidor. Verifique se a aplicação está rodando e tente novamente.'
    }
    switch (error.response.status) {
      case 400:
        return error.response.data?.message || 'Os dados informados são inválidos. Verifique e tente novamente.'
      case 401:
        return 'Usuário ou senha incorretos. Verifique os dados e tente novamente.'
      case 403:
        return 'Você não tem permissão para realizar esta ação.'
      case 404:
        return 'O recurso solicitado não foi encontrado.'
      case 409:
        return error.response.data?.message || 'Este registro já existe no sistema.'
      case 500:
        return 'Ocorreu um erro interno no servidor. Tente novamente em alguns instantes.'
      case 503:
        return 'O serviço está temporariamente indisponível. Tente novamente em alguns instantes.'
      default:
        return error.response.data?.message || 'Ocorreu um erro inesperado. Tente novamente.'
    }
  }
  return 'Ocorreu um erro inesperado. Tente novamente.'
}

export default api
