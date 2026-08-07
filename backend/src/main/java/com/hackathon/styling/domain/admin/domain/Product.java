package com.hackathon.styling.domain.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false)
    private String name;

    // 상품 카테고리
    @Column(nullable = false)
    private String category;

    // 색상
    @Column(nullable = false)
    private String color;

    // 사이즈
    @Column(nullable = false)
    private String size;

    // 가격
    @Column(nullable = false)
    private Integer price;

    // 상품 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 상품 이미지 URL
    private String imageUrl;

    // 재고
    @Column(nullable = false)
    private Integer stock;

    // 옷걸이 식별 코드
    @Column(nullable = false, unique = true)
    private String hangerCode;

    // 생성 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 상품 생성
    public Product(
            String name,
            String category,
            String color,
            String size,
            Integer price,
            String description,
            String imageUrl,
            Integer stock,
            String hangerCode
    ) {
        this.name = name;
        this.category = category;
        this.color = color;
        this.size = size;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.hangerCode = hangerCode;
    }

    // 상품 수정
    public void update(
            String name,
            String category,
            String color,
            String size,
            Integer price,
            String description,
            String imageUrl,
            Integer stock,
            String hangerCode
    ) {
        this.name = name;
        this.category = category;
        this.color = color;
        this.size = size;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.hangerCode = hangerCode;
    }
}