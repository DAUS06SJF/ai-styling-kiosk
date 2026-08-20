import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { createProduct, deleteProduct, getProducts, resolveProductImageUrl, updateProduct } from './api/products'
import { ProductForm } from './components/ProductForm'
import type { PageData, Product, ProductInput } from './types/product'

const emptyPage: PageData<Product> = {
  content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
}

function formatPrice(price: number) {
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(price)
}

function App() {
  const [data, setData] = useState<PageData<Product>>(emptyPage)
  const [keywordInput, setKeywordInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  const loadProducts = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setData(await getProducts({ keyword, category, page, size: 10 }))
    } catch (e) {
      setError(e instanceof Error ? e.message : '상품 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [keyword, category, page])

  useEffect(() => { void loadProducts() }, [loadProducts])

  const categories = useMemo(
    () => [...new Set(data.content.map((product) => product.category))].sort(),
    [data.content],
  )
  const lowStockCount = data.content.filter((product) => product.stock <= 3).length

  const search = (event: FormEvent) => {
    event.preventDefault()
    setPage(0)
    setKeyword(keywordInput.trim())
  }

  const openCreate = () => {
    setEditingProduct(null)
    setFormError('')
    setFormOpen(true)
  }

  const openEdit = (product: Product) => {
    setEditingProduct(product)
    setFormError('')
    setFormOpen(true)
  }

  const save = async (input: ProductInput) => {
    setSubmitting(true)
    setFormError('')
    try {
      if (editingProduct) await updateProduct(editingProduct.id, input)
      else await createProduct(input)
      setFormOpen(false)
      await loadProducts()
    } catch (e) {
      setFormError(e instanceof Error ? e.message : '상품을 저장하지 못했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  const remove = async (product: Product) => {
    if (!window.confirm(`“${product.name}” 상품을 삭제할까요? 이 작업은 되돌릴 수 없습니다.`)) return
    try {
      await deleteProduct(product.id)
      if (data.content.length === 1 && page > 0) setPage((current) => current - 1)
      else await loadProducts()
    } catch (e) {
      setError(e instanceof Error ? e.message : '상품을 삭제하지 못했습니다.')
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">A</span><span>ASK<small>KIOSK ADMIN</small></span></div>
        <nav aria-label="관리자 메뉴">
          <a className="nav-item active" href="#products"><span>◇</span>상품 관리</a>
        </nav>
      </aside>

      <main className="main-content" id="products">
        <header className="topbar">
          <div><p className="eyebrow">STORE OPERATIONS</p><h1>상품 관리</h1><p className="subtitle">키오스크에서 사용할 상품과 행거 연결 정보를 관리합니다.</p></div>
          <button className="button primary add-button" onClick={openCreate}><span>＋</span>새 상품 등록</button>
        </header>

        <section className="stats" aria-label="상품 현황">
          <article><span className="stat-label">전체 상품</span><strong>{data.totalElements.toLocaleString()}<small>개</small></strong><span className="stat-caption">등록된 상품 수</span></article>
          <article><span className="stat-label">현재 페이지 재고</span><strong>{data.content.reduce((sum, product) => sum + product.stock, 0).toLocaleString()}<small>개</small></strong><span className="stat-caption">실시간 집계</span></article>
          <article className={lowStockCount > 0 ? 'alert-stat' : ''}><span className="stat-label">재고 주의</span><strong>{lowStockCount}<small>개</small></strong><span className="stat-caption">재고 3개 이하</span></article>
        </section>

        <section className="panel">
          <div className="toolbar">
            <form className="search" onSubmit={search}>
              <span aria-hidden="true">⌕</span>
              <input value={keywordInput} onChange={(e) => setKeywordInput(e.target.value)} placeholder="상품명 또는 행거 코드 검색" aria-label="상품 검색" />
              <button type="submit">검색</button>
            </form>
            <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(0) }} aria-label="카테고리 필터">
              <option value="">전체 카테고리</option>
              {categories.map((item) => <option value={item} key={item}>{item}</option>)}
            </select>
            {(keyword || category) && <button className="text-button" onClick={() => { setKeyword(''); setKeywordInput(''); setCategory(''); setPage(0) }}>필터 초기화</button>}
          </div>

          {error && <div className="error-banner" role="alert"><span>{error}</span><button onClick={() => void loadProducts()}>다시 시도</button></div>}

          <div className="table-wrap">
            <table>
              <thead><tr><th>상품</th><th>카테고리</th><th>행거 코드</th><th>가격</th><th>재고</th><th><span className="sr-only">관리</span></th></tr></thead>
              <tbody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => <tr className="skeleton-row" key={i}><td colSpan={6}><span /></td></tr>)
                ) : data.content.length === 0 ? (
                  <tr><td colSpan={6}><div className="empty-state"><span>◇</span><strong>등록된 상품이 없습니다</strong><p>첫 상품을 등록하고 행거와 연결해 보세요.</p><button className="button primary" onClick={openCreate}>상품 등록하기</button></div></td></tr>
                ) : data.content.map((product) => (
                  <tr key={product.id}>
                    <td><div className="product-cell"><div className="thumbnail">{product.imageUrl ? <img src={resolveProductImageUrl(product.imageUrl)} alt="" /> : <span>{product.name.charAt(0)}</span>}</div><div><strong>{product.name}</strong><small>{product.color} · {product.size}</small></div></div></td>
                    <td><span className="tag">{product.category}</span></td>
                    <td><code>{product.hangerCode}</code></td>
                    <td className="price">{formatPrice(product.price)}</td>
                    <td><span className={`stock ${product.stock === 0 ? 'out' : product.stock <= 3 ? 'low' : ''}`}><i />{product.stock}개</span></td>
                    <td><div className="row-actions"><button onClick={() => openEdit(product)}>수정</button><button className="danger" onClick={() => void remove(product)}>삭제</button></div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <footer className="pagination">
            <span>총 {data.totalElements.toLocaleString()}개 중 {data.content.length ? page * data.size + 1 : 0}–{Math.min((page + 1) * data.size, data.totalElements)}개</span>
            <div><button disabled={data.first || loading} onClick={() => setPage((current) => current - 1)}>이전</button><strong>{data.totalPages === 0 ? 0 : page + 1} / {data.totalPages}</strong><button disabled={data.last || loading} onClick={() => setPage((current) => current + 1)}>다음</button></div>
          </footer>
        </section>
      </main>

      {formOpen && <ProductForm product={editingProduct} submitting={submitting} error={formError} onClose={() => !submitting && setFormOpen(false)} onSubmit={save} />}
    </div>
  )
}

export default App
