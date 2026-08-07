import { defineStore } from 'pinia'

import { tagService } from '@/services/tag.service'

import type {
  Tag,
  TagRequest,
} from '@/types/tags'

export const useTagStore = defineStore('tags', {
  state: () => ({
    tags: [] as Tag[],
    loading: false,
    saving: false,
    deleting: false,
    error: null as string | null,
  }),

  getters: {
    totalTags: (state) => state.tags.length,
  },

  actions: {
    async fetchTags() {
      this.loading = true
      this.error = null

      try {
        const response = await tagService.getAll()
        this.tags = response.data
      } catch {
        this.error = 'Impossible de charger les tags.'
      } finally {
        this.loading = false
      }
    },

    async createTag(data: TagRequest) {
      this.saving = true
      this.error = null

      try {
        const response = await tagService.create(data)
        this.tags.push(response.data)

        return response.data
      } finally {
        this.saving = false
      }
    },

    async updateTag(
      id: number,
      data: TagRequest,
    ) {
      this.saving = true
      this.error = null

      try {
        const response = await tagService.update(id, data)

        const index = this.tags.findIndex(
          (tag) => tag.id === id,
        )

        if (index !== -1) {
          this.tags[index] = response.data
        }

        return response.data
      } finally {
        this.saving = false
      }
    },

    async deleteTag(id: number) {
      this.deleting = true
      this.error = null

      try {
        await tagService.delete(id)

        this.tags = this.tags.filter(
          (tag) => tag.id !== id,
        )
      } finally {
        this.deleting = false
      }
    },
  },
})