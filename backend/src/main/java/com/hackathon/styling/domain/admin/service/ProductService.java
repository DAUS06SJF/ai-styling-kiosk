package com.hackathon.styling.domain.admin.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.dto.ProductCreateRequest;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.dto.ProductUpdateRequest;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 등록
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {

        if (productRepository.existsByHangerCode(request.getHangerCode())) {
            throw new IllegalArgumentException("이미 등록된 옷걸이 코드입니다.");
        }

        Product product = new Product(
                request.getName(),
                request.getCategory(),
                request.getColor(),
                request.getSize(),
                request.getPrice(),
                request.getDescription(),
                request.getImageUrl(),
                request.getStock(),
                request.getHangerCode()
        );

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    // 상품 전체 조회
    public List<ProductResponse> findAll() {

        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    // 상품 단건 조회
    public ProductResponse findById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 상품입니다. id=" + id)
                );

        return ProductResponse.from(product);
    }

    // 상품 수정
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 상품입니다. id=" + id)
                );

        if (!product.getHangerCode().equals(request.getHangerCode())
                && productRepository.existsByHangerCode(request.getHangerCode())) {

            throw new IllegalArgumentException("이미 등록된 옷걸이 코드입니다.");
        }

        product.update(
                request.getName(),
                request.getCategory(),
                request.getColor(),
                request.getSize(),
                request.getPrice(),
                request.getDescription(),
                request.getImageUrl(),
                request.getStock(),
                request.getHangerCode()
        );

        return ProductResponse.from(product);
    }

    // 상품 삭제
    @Transactional
    public void delete(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 상품입니다. id=" + id)
                );

        productRepository.delete(product);
    }
}