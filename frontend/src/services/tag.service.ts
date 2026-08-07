import api from '@/api/axios'

import type {
  Tag,
  TagRequest,
} from '@/types/tags'

export const tagService = {
  getAll() {
    return api.get<Tag[]>('/tags')
  },

  getById(id: number) {
    return api.get<Tag>(`/tags/${id}`)
  },

  create(data: TagRequest) {
    return api.post<Tag>('/tags', data)
  },

  update(id: number, data: TagRequest) {
    return api.put<Tag>(`/tags/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/tags/${id}`)
  },
}