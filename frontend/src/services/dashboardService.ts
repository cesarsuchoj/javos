import api from './api'
import { DashboardSummary } from '../types'

export const dashboardService = {
  getSummary: async (): Promise<DashboardSummary> => {
    const response = await api.get<DashboardSummary>('/dashboard/summary')
    return response.data
  },
}
