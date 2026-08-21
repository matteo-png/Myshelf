import api from '@/api/axios'
import type { Item } from '@/types/items'

import type { StatsGroup, StatsOverview, StatsTimeSeriesPoint } from '@/types/stats'

export const statsService = {
  getOverview() {
    return api.get<StatsOverview>('/stats/overview')
  },

  getItemsByCollection() {
    return api.get<StatsGroup[]>('/stats/items-by-collection')
  },

  getItemsByCategory() {
    return api.get<StatsGroup[]>('/stats/items-by-category')
  },

  getItemsByPurchasePlace() {
    return api.get<StatsGroup[]>('/stats/items-by-purchase-place')
  },

  getItemsByStatus() {
    return api.get<StatsGroup[]>('/stats/items-by-status')
  },

  getItemsByMonth(year: number) {
    return api.get<StatsTimeSeriesPoint[]>('/stats/items-by-month', {
      params: { year },
    })
  },

  getItemsByYear() {
    return api.get<StatsTimeSeriesPoint[]>('/stats/items-by-year')
  },

  getRecentItems() {
    return api.get<Item[]>('/stats/recent-items')
  },
}
