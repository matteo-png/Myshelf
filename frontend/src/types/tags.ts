export interface Tag {
  id: number
  name: string
  itemCount: number
  createdAt: string
  updatedAt: string
}

export interface TagRequest {
  name: string
}