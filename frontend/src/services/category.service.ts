import api from '@/api/axios'

import type {
  Category,
  CategoryRequest,
} from '@/types/category'

export const categoryService = {
  getAll() {
    return api.get<Category[]>('/categories')
  },

  getById(id: number) {
    return api.get<Category>(`/categories/${id}`)
  },

  create(data: CategoryRequest) {
    return api.post<Category>('/categories', data)
  },

  update(id: number, data: CategoryRequest) {
    return api.put<Category>(`/categories/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/categories/${id}`)
  },
}