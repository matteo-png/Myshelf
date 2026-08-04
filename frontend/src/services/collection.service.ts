import api from '@/api/axios'

import type {
  Collection,
  CollectionRequest,
} from '@/types/collections'

export const collectionService = {
  getAll() {
    return api.get<Collection[]>('/collections')
  },

  getById(id: number) {
    return api.get<Collection>(`/collections/${id}`)
  },

  create(data: CollectionRequest) {
    return api.post<Collection>('/collections', data)
  },

  update(id: number, data: CollectionRequest) {
    return api.put<Collection>(`/collections/${id}`, data)
  },

  delete(id: number) {
    return api.delete(`/collections/${id}`)
  },
}