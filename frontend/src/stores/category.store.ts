import { defineStore } from 'pinia'

import { categoryService } from '@/services/category.service'

import type {
  Category,
  CategoryRequest,
} from '@/types/category'

export const useCategoryStore = defineStore('categories', {
  state: () => ({
    categories: [] as Category[],
    loading: false,
    saving: false,
    deleting: false,
    error: null as string | null,
  }),

  getters: {
    totalCategories: (state) => state.categories.length,
  },

  actions: {
    async fetchCategories() {
      this.loading = true
      this.error = null

      try {
        const response = await categoryService.getAll()
        this.categories = response.data
      } catch {
        this.error = 'Impossible de charger les catégories.'
      } finally {
        this.loading = false
      }
    },

    async createCategory(data: CategoryRequest) {
      this.saving = true
      this.error = null

      try {
        const response = await categoryService.create(data)
        this.categories.push(response.data)

        return response.data
      } finally {
        this.saving = false
      }
    },

    async updateCategory(
      id: number,
      data: CategoryRequest,
    ) {
      this.saving = true
      this.error = null

      try {
        const response = await categoryService.update(id, data)

        const index = this.categories.findIndex(
          (category) => category.id === id,
        )

        if (index !== -1) {
          this.categories[index] = response.data
        }

        return response.data
      } finally {
        this.saving = false
      }
    },

    async deleteCategory(id: number) {
      this.deleting = true
      this.error = null

      try {
        await categoryService.delete(id)

        this.categories = this.categories.filter(
          (category) => category.id !== id,
        )
      } finally {
        this.deleting = false
      }
    },
  },
})