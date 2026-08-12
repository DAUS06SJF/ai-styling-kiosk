import type { PageData, Product, ProductInput } from '../types/product'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

interface ApiResponse<T> {
  success: boolean
  data: T
  error: { code: string; message: string } | null
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  })

  const body = (await response.json().catch(() => null)) as ApiResponse<T> | null
  if (!response.ok || !body?.success) {
    throw new Error(body?.error?.message ?? '서버 요청을 처리하지 못했습니다.')
  }
  return body.data
}

export interface ProductQuery {
  keyword?: string
  category?: string
  page?: number
  size?: number
}

export function getProducts(query: ProductQuery): Promise<PageData<Product>> {
  const params = new URLSearchParams({
    page: String(query.page ?? 0),
    size: String(query.size ?? 10),
    sort: 'createdAt,desc',
  })
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.category) params.set('category', query.category)
  return request(`/admin/products?${params}`)
}

export function createProduct(input: ProductInput): Promise<Product> {
  return request('/admin/products', { method: 'POST', body: JSON.stringify(input) })
}

export function updateProduct(id: number, input: ProductInput): Promise<Product> {
  return request(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(input) })
}

export function deleteProduct(id: number): Promise<void> {
  return request(`/admin/products/${id}`, { method: 'DELETE' })
}
