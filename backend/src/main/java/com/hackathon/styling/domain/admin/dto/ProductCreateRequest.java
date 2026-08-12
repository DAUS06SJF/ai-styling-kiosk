package com.hackathon.styling.domain.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
    private String category;

    @NotBlank(message = "색상은 필수입니다.")
    @Size(max = 50, message = "색상은 50자 이하여야 합니다.")
    private String color;

    @NotBlank(message = "사이즈는 필수입니다.")
    @Size(max = 30, message = "사이즈는 30자 이하여야 합니다.")
    private String size;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Size(max = 2000, message = "상품 설명은 2000자 이하여야 합니다.")
    private String description;

    @Size(max = 1000, message = "이미지 URL은 1000자 이하여야 합니다.")
    private String imageUrl;

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    private Integer stock;

    @NotBlank(message = "옷걸이 코드는 필수입니다.")
    @Size(max = 100, message = "옷걸이 코드는 100자 이하여야 합니다.")
    private String hangerCode;
}
