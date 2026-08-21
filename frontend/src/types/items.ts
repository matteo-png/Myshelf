export const ITEM_STATUSES = ['ACTIVE', 'SOLD', 'LOST', 'GIVEN', 'OTHER'] as const

export type ItemStatus = (typeof ITEM_STATUSES)[number]

export interface Item {
  id: number

  collectionId: number
  collectionName: string

  categoryId: number | null
  categoryName: string | null

  purchasePlaceId: number | null
  purchasePlaceName: string | null

  name: string
  description: string | null
  estimatedValue: number | null
  purchaseDate: string | null
  purchaseUrl: string | null
  status: ItemStatus

  fileName: string | null
  fileContentType: string | null
  fichierUrl: string | null

  tags: string[]

  createdAt: string
  updatedAt: string
}

export interface ItemRequest {
  collectionId: number
  categoryId: number | null
  purchasePlaceId: number | null

  name: string
  description: string | null
  estimatedValue: number | null
  purchaseDate: string | null
  purchaseUrl: string | null
  status: ItemStatus | null

  tagIds: number[]

  removeFile?: boolean
}
