import api from '@/api/axios'

import type { Item, ItemRequest } from '@/types/items'

export const itemService = {
  getAll(collectionId?: number | null) {
    return api.get<Item[]>('/items', {
      params: collectionId
        ? {
            collectionId,
          }
        : undefined,
    })
  },

  getById(id: number) {
    return api.get<Item>(`/items/${id}`)
  },

  create(data: ItemRequest) {
    return api.post<Item>('/items', data)
  },

  update(id: number, data: ItemRequest) {
    return api.put<Item>(`/items/${id}`, data)
  },

  delete(id: number) {
    return api.delete(`/items/${id}`)
  },
}