import type { PageData, Product, ProductInput } from '../types/product'

const browserBackendUrl = `${window.location.protocol}//${window.location.hostname}:8080`
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? browserBackendUrl).replace(/\/$/, '')

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

export function resolveProductImageUrl(imageUrl: string | null): string {
  if (!imageUrl) return ''

  try {
    const image = new URL(imageUrl, API_BASE_URL)
    const api = new URL(API_BASE_URL)
    if (["localhost", "127.0.0.1"].includes(image.hostname)
      && !["localhost", "127.0.0.1"].includes(api.hostname)) {
      image.protocol = api.protocol
      image.host = api.host
    }
    return image.toString()
  } catch {
    return imageUrl
  }
}
