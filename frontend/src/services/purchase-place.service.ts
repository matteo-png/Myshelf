import api from '@/api/axios'

import type {
  PurchasePlace,
  PurchasePlaceRequest,
} from '@/types/purchase-place'

export const purchasePlaceService = {
  getAll() {
    return api.get<PurchasePlace[]>('/purchase-places')
  },

  getById(id: number) {
    return api.get<PurchasePlace>(`/purchase-places/${id}`)
  },

  create(data: PurchasePlaceRequest) {
    return api.post<PurchasePlace>('/purchase-places', data)
  },

  update(id: number, data: PurchasePlaceRequest) {
    return api.put<PurchasePlace>(`/purchase-places/${id}`, data)
  },

  delete(id: number) {
    return api.delete<void>(`/purchase-places/${id}`)
  },
}