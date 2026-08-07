export const PURCHASE_PLACE_TYPES = [
  'OTHER',
  'MAGASIN',
  'MARKETPLACE',
  'ONLINE',
] as const

export type PurchasePlaceType =
  (typeof PURCHASE_PLACE_TYPES)[number]

export interface PurchasePlace {
  id: number
  name: string
  type: PurchasePlaceType | null
  websiteUrl: string | null
  itemCount: number
  createdAt: string
  updatedAt: string
}

export interface PurchasePlaceRequest {
  name: string
  type: PurchasePlaceType | null
  websiteUrl: string | null
}