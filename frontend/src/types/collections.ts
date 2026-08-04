export interface Collection {
  id: number
  name: string
  description: string | null
  itemCount: number
  createdAt: string
  updatedAt: string
}

export interface CollectionRequest {
  name: string
  description: string | null
}