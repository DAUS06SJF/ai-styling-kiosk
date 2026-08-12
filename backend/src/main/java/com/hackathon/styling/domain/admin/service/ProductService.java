package com.hackathon.styling.domain.admin.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.dto.ProductCreateRequest;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.dto.ProductUpdateRequest;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.global.common.PageResponse;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 등록
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        String hangerCode = normalizeRequired(request.getHangerCode());

        validateUniqueHangerCode(hangerCode, null);

        Product product = new Product(
                normalizeRequired(request.getName()),
                normalizeRequired(request.getCategory()),
                normalizeRequired(request.getColor()),
                normalizeRequired(request.getSize()),
                request.getPrice(),
                normalizeOptional(request.getDescription()),
                normalizeOptional(request.getImageUrl()),
                request.getStock(),
                hangerCode
        );

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    // 상품 목록 조회
    public PageResponse<ProductResponse> findAll(String keyword, String category, Pageable pageable) {
        return PageResponse.from(
                productRepository.search(normalizeOptional(keyword), normalizeOptional(category), pageable)
                        .map(ProductResponse::from)
        );
    }

    // 상품 단건 조회
    public ProductResponse findById(Long id) {

        return ProductResponse.from(findProduct(id));
    }

    // 상품 수정
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {

        Product product = findProduct(id);
        String hangerCode = normalizeRequired(request.getHangerCode());
        validateUniqueHangerCode(hangerCode, product.getId());

        product.update(
                normalizeRequired(request.getName()),
                normalizeRequired(request.getCategory()),
                normalizeRequired(request.getColor()),
                normalizeRequired(request.getSize()),
                request.getPrice(),
                normalizeOptional(request.getDescription()),
                normalizeOptional(request.getImageUrl()),
                request.getStock(),
                hangerCode
        );

        return ProductResponse.from(product);
    }

    // 상품 삭제
    @Transactional
    public void delete(Long id) {

        productRepository.delete(findProduct(id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "상품 정보를 찾을 수 없습니다. id=" + id
                ));
    }

    private void validateUniqueHangerCode(String hangerCode, Long currentProductId) {
        productRepository.findByHangerCodeIgnoreCase(hangerCode)
                .filter(product -> !Objects.equals(product.getId(), currentProductId))
                .ifPresent(product -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_HANGER_CODE);
                });
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
