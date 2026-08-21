import { defineStore } from 'pinia'

import { statsService } from '@/services/stats.service'

import type { StatsGroup, StatsOverview, StatsTimeSeriesPoint } from '@/types/stats'
import type { Item } from '@/types/items'

function createEmptyOverview(): StatsOverview {
  return {
    collectionsCount: 0,
    itemsCount: 0,
    categoriesCount: 0,
    tagsCount: 0,
    purchasePlacesCount: 0,
    totalEstimatedValue: 0,
  }
}

export const useStatsStore = defineStore('stats', {
  state: () => ({
    overview: createEmptyOverview(),

    itemsByCollection: [] as StatsGroup[],
    itemsByCategory: [] as StatsGroup[],
    itemsByPurchasePlace: [] as StatsGroup[],
    itemsByStatus: [] as StatsGroup[],

    itemsByMonth: [] as StatsTimeSeriesPoint[],
    itemsByYear: [] as StatsTimeSeriesPoint[],
    recentItems: [] as Item[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchDashboard(year: number) {
      this.loading = true
      this.error = null

      try {
        const [
          overviewResponse,
          collectionsResponse,
          categoriesResponse,
          purchasePlacesResponse,
          statusResponse,
          monthsResponse,
          yearsResponse,
          recentItems,
        ] = await Promise.all([
          statsService.getOverview(),
          statsService.getItemsByCollection(),
          statsService.getItemsByCategory(),
          statsService.getItemsByPurchasePlace(),
          statsService.getItemsByStatus(),
          statsService.getItemsByMonth(year),
          statsService.getItemsByYear(),
          statsService.getRecentItems(),
        ])

        this.overview = overviewResponse.data
        this.itemsByCollection = collectionsResponse.data
        this.itemsByCategory = categoriesResponse.data
        this.itemsByPurchasePlace = purchasePlacesResponse.data
        this.itemsByStatus = statusResponse.data
        this.itemsByMonth = monthsResponse.data
        this.itemsByYear = yearsResponse.data
        this.recentItems = recentItems.data
      } catch {
        this.error = 'Impossible de charger les statistiques du tableau de bord.'
      } finally {
        this.loading = false
      }
    },

    async fetchItemsByMonth(year: number) {
      this.error = null

      try {
        const response = await statsService.getItemsByMonth(year)
        this.itemsByMonth = response.data
      } catch {
        this.error = 'Impossible de charger les acquisitions pour cette année.'
      }
    },
  },
})
