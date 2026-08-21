import api from '@/api/axios'

import type { Item, ItemRequest } from '@/types/items'

function buildMultipartData(data: ItemRequest, file?: File | null) {
  const formData = new FormData()

  formData.append('item', new Blob([JSON.stringify(data)], { type: 'application/json' }))

  if (file) {
    formData.append('file', file)
  }

  return formData
}

export const itemService = {
  getAll(collectionId?: number | null) {
    return api.get<Item[]>('/items', {
      params: collectionId ? { collectionId } : undefined,
    })
  },

  getById(id: number) {
    return api.get<Item>(`/items/${id}`)
  },

  create(data: ItemRequest, file?: File | null) {
    if (!file) {
      return api.post<Item>('/items', data)
    }

    return api.post<Item>('/items', buildMultipartData(data, file))
  },

  update(id: number, data: ItemRequest, file?: File | null) {
    /*
     * Si on ajoute/remplace un fichier, multipart.
     * Si on demande juste sa suppression, le JSON classique suffit
     * puisque removeFile fait partie de ItemRequest.
     */
    if (!file) {
      return api.put<Item>(`/items/${id}`, data)
    }

    return api.put<Item>(`/items/${id}`, buildMultipartData(data, file))
  },

  delete(id: number) {
    return api.delete<void>(`/items/${id}`)
  },

  downloadFile(id: number) {
    return api.get<Blob>(`/items/${id}/file`, {
      responseType: 'blob',
    })
  },
}
