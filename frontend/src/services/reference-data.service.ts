import api from '@/api/axios'

import type { Category } from '@/types/category'
import type { Collection } from '@/types/collections'
import type { PurchasePlace } from '@/types/purchase-place'
import type { Tag } from '@/types/tags'

export const referenceDataService = {
  getCollections() {
    return api.get<Collection[]>('/collections')
  },

  getCategories() {
    return api.get<Category[]>('/categories')
  },

  getTags() {
    return api.get<Tag[]>('/tags')
  },

  getPurchasePlaces() {
    return api.get<PurchasePlace[]>('/purchase-places')
  },
}