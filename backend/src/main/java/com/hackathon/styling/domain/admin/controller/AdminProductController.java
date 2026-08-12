package com.hackathon.styling.domain.admin.controller;

import com.hackathon.styling.domain.admin.dto.ProductCreateRequest;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.dto.ProductUpdateRequest;
import com.hackathon.styling.domain.admin.service.ProductService;
import com.hackathon.styling.global.common.ApiResponse;
import com.hackathon.styling.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // 상품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 상품 목록 조회 (검색/카테고리 필터/페이지네이션)
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        return ApiResponse.success(productService.findAll(keyword, category, pageable));
    }

    // 상품 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(
            @PathVariable Long id
    ) {
        return ApiResponse.success(productService.findById(id));
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ApiResponse.success(productService.update(id, request));
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        productService.delete(id);
        return ApiResponse.success();
    }
}
