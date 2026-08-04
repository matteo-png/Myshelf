import { defineStore } from 'pinia'

import { collectionService } from '@/services/collection.service'

import type {
  Collection,
  CollectionRequest,
} from '@/types/collections'

export const useCollectionStore = defineStore('collections', {
  state: () => ({
    collections: [] as Collection[],
    loading: false,
    saving: false,
    deleting: false,
    error: null as string | null,
  }),

  getters: {
    totalCollections: (state) => state.collections.length,
  },

  actions: {
    async fetchCollections() {
      this.loading = true
      this.error = null

      try {
        const response = await collectionService.getAll()
        this.collections = response.data
      } catch {
        this.error = 'Impossible de charger les collections.'
      } finally {
        this.loading = false
      }
    },

    async createCollection(data: CollectionRequest) {
      this.saving = true
      this.error = null

      try {
        const response = await collectionService.create(data)
        this.collections.push(response.data)

        return response.data
      } finally {
        this.saving = false
      }
    },

    async updateCollection(
      id: number,
      data: CollectionRequest,
    ) {
      this.saving = true
      this.error = null

      try {
        const response = await collectionService.update(id, data)

        const index = this.collections.findIndex(
          (collection) => collection.id === id,
        )

        if (index !== -1) {
          this.collections[index] = response.data
        }

        return response.data
      } finally {
        this.saving = false
      }
    },

    async deleteCollection(id: number) {
      this.deleting = true
      this.error = null

      try {
        await collectionService.delete(id)

        this.collections = this.collections.filter(
          (collection) => collection.id !== id,
        )
      } finally {
        this.deleting = false
      }
    },
  },
})