import { useEffect, useState, type FormEvent } from 'react'
import type { Product, ProductInput } from '../types/product'

const emptyProduct: ProductInput = {
  name: '',
  category: '',
  color: '',
  size: '',
  price: 0,
  description: '',
  imageUrl: '',
  stock: 0,
  hangerCode: '',
}

interface ProductFormProps {
  product: Product | null
  submitting: boolean
  error: string
  onClose: () => void
  onSubmit: (input: ProductInput) => Promise<void>
}

export function ProductForm({ product, submitting, error, onClose, onSubmit }: ProductFormProps) {
  const [form, setForm] = useState<ProductInput>(emptyProduct)

  useEffect(() => {
    setForm(product ? {
      name: product.name,
      category: product.category,
      color: product.color,
      size: product.size,
      price: product.price,
      description: product.description ?? '',
      imageUrl: product.imageUrl ?? '',
      stock: product.stock,
      hangerCode: product.hangerCode,
    } : emptyProduct)
  }, [product])

  const updateField = <K extends keyof ProductInput>(key: K, value: ProductInput[K]) => {
    setForm((current) => ({ ...current, [key]: value }))
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    await onSubmit({
      ...form,
      name: form.name.trim(),
      category: form.category.trim(),
      color: form.color.trim(),
      size: form.size.trim(),
      hangerCode: form.hangerCode.trim(),
      description: form.description?.trim() || null,
      imageUrl: form.imageUrl?.trim() || null,
    })
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="modal" role="dialog" aria-modal="true" aria-labelledby="product-form-title" onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <p className="eyebrow">PRODUCT EDITOR</p>
            <h2 id="product-form-title">{product ? '상품 정보 수정' : '새 상품 등록'}</h2>
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="닫기">×</button>
        </div>

        <form onSubmit={submit}>
          <div className="form-grid">
            <label className="field field-wide">
              <span>상품명 *</span>
              <input required maxLength={100} value={form.name} onChange={(e) => updateField('name', e.target.value)} placeholder="예: 오버핏 코튼 셔츠" />
            </label>
            <label className="field">
              <span>카테고리 *</span>
              <input required maxLength={50} list="category-options" value={form.category} onChange={(e) => updateField('category', e.target.value)} placeholder="상의" />
              <datalist id="category-options">
                <option value="상의" /><option value="하의" /><option value="아우터" /><option value="원피스" /><option value="신발" /><option value="액세서리" />
              </datalist>
            </label>
            <label className="field">
              <span>행거 코드 *</span>
              <input required maxLength={100} value={form.hangerCode} onChange={(e) => updateField('hangerCode', e.target.value)} placeholder="H-001" />
            </label>
            <label className="field">
              <span>색상 *</span>
              <input required maxLength={50} value={form.color} onChange={(e) => updateField('color', e.target.value)} placeholder="Ivory" />
            </label>
            <label className="field">
              <span>사이즈 *</span>
              <input required maxLength={30} value={form.size} onChange={(e) => updateField('size', e.target.value)} placeholder="M" />
            </label>
            <label className="field">
              <span>가격 *</span>
              <input required type="number" min={0} value={form.price} onChange={(e) => updateField('price', Number(e.target.value))} />
            </label>
            <label className="field">
              <span>재고 *</span>
              <input required type="number" min={0} value={form.stock} onChange={(e) => updateField('stock', Number(e.target.value))} />
            </label>
            <label className="field field-wide">
              <span>이미지 URL</span>
              <input type="url" maxLength={1000} value={form.imageUrl ?? ''} onChange={(e) => updateField('imageUrl', e.target.value)} placeholder="https://..." />
            </label>
            <label className="field field-wide">
              <span>상품 설명</span>
              <textarea maxLength={2000} rows={4} value={form.description ?? ''} onChange={(e) => updateField('description', e.target.value)} placeholder="소재, 핏, 스타일링 포인트를 입력하세요." />
            </label>
          </div>

          {error && <p className="form-error" role="alert">{error}</p>}
          <div className="modal-actions">
            <button className="button secondary" type="button" onClick={onClose}>취소</button>
            <button className="button primary" type="submit" disabled={submitting}>{submitting ? '저장 중…' : product ? '변경사항 저장' : '상품 등록'}</button>
          </div>
        </form>
      </section>
    </div>
  )
}
