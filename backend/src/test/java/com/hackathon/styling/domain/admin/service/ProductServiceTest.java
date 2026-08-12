package com.hackathon.styling.domain.admin.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.dto.ProductCreateRequest;
import com.hackathon.styling.domain.admin.dto.ProductResponse;
import com.hackathon.styling.domain.admin.dto.ProductUpdateRequest;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.global.common.PageResponse;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("상품을 등록할 때 문자열 앞뒤 공백을 제거한다")
    void createProduct() {
        ProductCreateRequest request = createRequest("  셔츠  ", "  H-001  ");
        when(productRepository.findByHangerCodeIgnoreCase("H-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("셔츠");
        assertThat(captor.getValue().getHangerCode()).isEqualTo("H-001");
        assertThat(response.getName()).isEqualTo("셔츠");
    }

    @Test
    @DisplayName("이미 사용 중인 행거 코드로 상품을 등록할 수 없다")
    void rejectDuplicateHangerCode() {
        Product existing = mock(Product.class);
        when(productRepository.findByHangerCodeIgnoreCase("H-001")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.create(createRequest("셔츠", "H-001")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_HANGER_CODE));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 목록을 페이지 형식으로 조회한다")
    void findAllProducts() {
        Product product = product("셔츠", "H-001");
        PageRequest pageable = PageRequest.of(0, 10);
        when(productRepository.search("셔츠", "상의", pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        PageResponse<ProductResponse> response = productService.findAll(" 셔츠 ", " 상의 ", pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("셔츠");
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회는 PRODUCT_NOT_FOUND 오류를 반환한다")
    void productNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 정보를 수정한다")
    void updateProduct() {
        Product product = product("셔츠", "H-001");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByHangerCodeIgnoreCase("H-002")).thenReturn(Optional.empty());

        ProductResponse response = productService.update(1L, updateRequest("재킷", "H-002"));

        assertThat(response.getName()).isEqualTo("재킷");
        assertThat(response.getHangerCode()).isEqualTo("H-002");
        assertThat(response.getPrice()).isEqualTo(120000);
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleteProduct() {
        Product product = product("셔츠", "H-001");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    private ProductCreateRequest createRequest(String name, String hangerCode) {
        return new ProductCreateRequest(
                name, "상의", "아이보리", "M", 59000,
                "코튼 셔츠", "https://example.com/shirt.jpg", 10, hangerCode
        );
    }

    private ProductUpdateRequest updateRequest(String name, String hangerCode) {
        return new ProductUpdateRequest(
                name, "아우터", "검정", "L", 120000,
                "울 재킷", "https://example.com/jacket.jpg", 4, hangerCode
        );
    }

    private Product product(String name, String hangerCode) {
        return new Product(
                name, "상의", "아이보리", "M", 59000,
                "코튼 셔츠", "https://example.com/shirt.jpg", 10, hangerCode
        );
    }
}
