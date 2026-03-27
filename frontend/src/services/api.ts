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

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

/**
 * Retorna uma mensagem de erro amigável baseada no tipo de falha da requisição.
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
