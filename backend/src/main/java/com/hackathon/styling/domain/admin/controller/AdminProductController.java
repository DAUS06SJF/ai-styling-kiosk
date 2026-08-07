package com.hackathon.styling.domain.admin.controller;

import com.hackathon.styling.domain.admin.dto.ProductCreateRequest;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.dto.ProductUpdateRequest;
import com.hackathon.styling.domain.admin.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // 상품 등록
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 상품 전체 조회
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {

        List<ProductResponse> response = productService.findAll();

        return ResponseEntity.ok(response);
    }

    // 상품 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable Long id
    ) {
        ProductResponse response = productService.findById(id);

        return ResponseEntity.ok(response);
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.update(id, request);

        return ResponseEntity.ok(response);
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        productService.delete(id);

        return ResponseEntity.noContent().build();
    }
}