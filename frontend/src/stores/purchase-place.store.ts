import { defineStore } from 'pinia'

import { purchasePlaceService } from '@/services/purchase-place.service'

import type {
  PurchasePlace,
  PurchasePlaceRequest,
} from '@/types/purchase-place'

export const usePurchasePlaceStore = defineStore('purchasePlaces', {
  state: () => ({
    purchasePlaces: [] as PurchasePlace[],
    loading: false,
    saving: false,
    deleting: false,
    error: null as string | null,
  }),

  getters: {
    totalPurchasePlaces: (state) => state.purchasePlaces.length,
  },

  actions: {
    async fetchPurchasePlaces() {
      this.loading = true
      this.error = null

      try {
        const response = await purchasePlaceService.getAll()
        this.purchasePlaces = response.data
      } catch {
        this.error = 'Impossible de charger les lieux d’achat.'
      } finally {
        this.loading = false
      }
    },

    async createPurchasePlace(data: PurchasePlaceRequest) {
      this.saving = true
      this.error = null

      try {
        const response = await purchasePlaceService.create(data)
        this.purchasePlaces.push(response.data)

        return response.data
      } finally {
        this.saving = false
      }
    },

    async updatePurchasePlace(
      id: number,
      data: PurchasePlaceRequest,
    ) {
      this.saving = true
      this.error = null

      try {
        const response = await purchasePlaceService.update(id, data)

        const index = this.purchasePlaces.findIndex(
          (place) => place.id === id,
        )

        if (index !== -1) {
          this.purchasePlaces[index] = response.data
        }

        return response.data
      } finally {
        this.saving = false
      }
    },

    async deletePurchasePlace(id: number) {
      this.deleting = true
      this.error = null

      try {
        await purchasePlaceService.delete(id)

        this.purchasePlaces = this.purchasePlaces.filter(
          (place) => place.id !== id,
        )
      } finally {
        this.deleting = false
      }
    },
  },
})