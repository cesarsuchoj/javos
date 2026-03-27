import api from './api'
import { DashboardSummary } from '../types'

export const dashboardService = {
  getSummary: async (): Promise<DashboardSummary> => {
    const response = await api.get<DashboardSummary>('/v1/dashboard/summary')
    return response.data
  },
}
