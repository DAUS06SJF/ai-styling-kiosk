package com.hackathon.styling.domain.admin.dto;

import com.hackathon.styling.domain.admin.domain.Product;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final String color;
    private final String size;
    private final Integer price;
    private final String description;
    private final String imageUrl;
    private final Integer stock;
    private final String hangerCode;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.category = product.getCategory();
        this.color = product.getColor();
        this.size = product.getSize();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.imageUrl = product.getImageUrl();
        this.stock = product.getStock();
        this.hangerCode = product.getHangerCode();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(product);
    }
}