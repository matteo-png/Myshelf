import { defineStore } from 'pinia'

import { itemService } from '@/services/item.service'

import type { Item, ItemRequest } from '@/types/items'

export const useItemStore = defineStore('items', {
  state: () => ({
    items: [] as Item[],
    loading: false,
    saving: false,
    deleting: false,
    error: null as string | null,
  }),

  getters: {
    totalItems: (state) => state.items.length,
  },

  actions: {
    async fetchItems(collectionId?: number | null) {
      this.loading = true
      this.error = null

      try {
        const response = await itemService.getAll(collectionId)
        this.items = response.data
      } catch {
        this.error = 'Impossible de charger les objets.'
      } finally {
        this.loading = false
      }
    },

    async createItem(data: ItemRequest, file?: File | null) {
      this.saving = true
      this.error = null

      try {
        const response = await itemService.create(data, file)
        this.items.push(response.data)

        return response.data
      } finally {
        this.saving = false
      }
    },

    async updateItem(id: number, data: ItemRequest, file?: File | null) {
      this.saving = true
      this.error = null

      try {
        const response = await itemService.update(id, data, file)

        const index = this.items.findIndex((item) => item.id === id)

        if (index !== -1) {
          this.items[index] = response.data
        }

        return response.data
      } finally {
        this.saving = false
      }
    },

    async deleteItem(id: number) {
      this.deleting = true
      this.error = null

      try {
        await itemService.delete(id)

        this.items = this.items.filter((item) => item.id !== id)
      } finally {
        this.deleting = false
      }
    },
  },
})
