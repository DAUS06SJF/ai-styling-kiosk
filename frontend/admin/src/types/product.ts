export interface Product {
  id: number
  name: string
  category: string
  color: string
  size: string
  price: number
  description: string | null
  imageUrl: string | null
  stock: number
  hangerCode: string
  createdAt: string
  updatedAt: string
}

export type ProductInput = Omit<Product, 'id' | 'createdAt' | 'updatedAt'>

export interface PageData<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
