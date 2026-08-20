package com.hackathon.styling.domain.styling.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.domain.styling.client.StylingAiClient;
import com.hackathon.styling.domain.styling.client.StylingAiInput;
import com.hackathon.styling.domain.styling.client.StylingAiOutput;
import com.hackathon.styling.domain.styling.client.StylingImageClient;
import com.hackathon.styling.domain.styling.client.BuiltInStylingImageFallback;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.domain.styling.domain.StylingRecommendation;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.domain.styling.repository.StylingRecommendationRepository;
import com.hackathon.styling.global.error.BusinessException;
import com.hackathon.styling.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StylingRecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StylingAiClient stylingAiClient;

    @Mock
    private StylingRecommendationRepository stylingRecommendationRepository;

    @Mock
    private StylingImageClient stylingImageClient;

    @Mock
    private BuiltInStylingImageFallback builtInStylingImageFallback;

    private StylingRecommendationService service;

    @BeforeEach
    void setUp() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setCandidateLimit(24);
        properties.setRecommendationCount(3);
        service = new StylingRecommendationService(
                productRepository,
                stylingRecommendationRepository,
                stylingAiClient,
                stylingImageClient,
                builtInStylingImageFallback,
                properties
        );
    }

    @Test
    @DisplayName("행거 상품을 고정한 서로 다른 AI 코디 네 건을 생성한다")
    void recommendStyling() {
        AtomicLong generatedId = new AtomicLong();
        when(stylingRecommendationRepository.saveAndFlush(any(StylingRecommendation.class)))
                .thenAnswer(invocation -> {
                    StylingRecommendation styling = invocation.getArgument(0);
                    ReflectionTestUtils.setField(styling, "id", generatedId.incrementAndGet());
                    return styling;
                });
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        Product shirt = product(2L, "로고 티셔츠", "TSHIRT_TOP", "White", "H-0002");
        Product shoes = product(3L, "레더 스니커즈", "SHOES", "Black", "H-0003");
        Product pants = product(4L, "와이드 팬츠", "PANTS", "Gray", "H-0004");
        Product cap = product(5L, "로고 캡", "CAP", "Black", "H-0005");

        when(productRepository.findByHangerCodeIgnoreCase("H-0001")).thenReturn(Optional.of(selected));
        when(productRepository.findAvailableForStyling(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(shirt, shoes, pants, cap));
        when(stylingAiClient.generate(any(StylingAiInput.class))).thenAnswer(invocation -> {
            StylingAiInput input = invocation.getArgument(0);
            Long productId = input.candidateProducts().get(0).productId();
            return new StylingAiOutput(
                    "모던 모노크롬 룩 " + input.variantIndex(),
                    "선택 상품을 유지한 서로 다른 조합입니다.",
                    List.of(new StylingAiOutput.Recommendation(productId, "선택 상품과 조화롭습니다."))
            );
        });
        when(stylingImageClient.generate(any())).thenReturn(new byte[]{1, 2, 3});

        StylingRecommendationResponse response = service.recommend(request(" H-0001 "));

        assertThat(response.lookName()).isEqualTo("모던 모노크롬 룩 4");
        assertThat(response.selectedProduct().getId()).isEqualTo(1L);
        assertThat(response.occasion()).isEqualTo("데이트");
        assertThat(response.preferredColors()).containsExactly("검정", "흰색");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).product().getId()).isEqualTo(5L);
        assertThat(response.kodi()).isEqualTo("/api/styling/recommendations/4/image");
        assertThat(response.kodiSelected()).isNull();
        verify(stylingRecommendationRepository, times(4))
                .saveAndFlush(any(StylingRecommendation.class));

        ArgumentCaptor<StylingAiInput> captor = ArgumentCaptor.forClass(StylingAiInput.class);
        verify(stylingAiClient, times(4)).generate(captor.capture());
        assertThat(captor.getAllValues()).extracting(StylingAiInput::variantIndex)
                .containsExactly(1, 2, 3, 4);
        assertThat(captor.getAllValues().get(0).candidateProducts())
                .extracting(StylingAiInput.AiProduct::productId)
                .containsExactly(2L, 3L, 4L, 5L);
        assertThat(captor.getAllValues().get(3).candidateProducts())
                .extracting(StylingAiInput.AiProduct::productId)
                .containsExactly(5L);

        ArgumentCaptor<com.hackathon.styling.domain.styling.client.StylingImageInput> imageCaptor =
                ArgumentCaptor.forClass(com.hackathon.styling.domain.styling.client.StylingImageInput.class);
        verify(stylingImageClient, times(4)).generate(imageCaptor.capture());
        assertThat(imageCaptor.getAllValues()).extracting(
                com.hackathon.styling.domain.styling.client.StylingImageInput::variantIndex
        ).containsExactlyInAnyOrder(1, 2, 3, 4);
        assertThat(imageCaptor.getAllValues())
                .allSatisfy(input -> assertThat(input.selectedProduct().imageUrl())
                        .isEqualTo("https://example.com/product.jpg"));
    }

    @Test
    @DisplayName("등록되지 않은 행거는 HANGER_NOT_FOUND 오류를 반환한다")
    void hangerNotFound() {
        when(productRepository.findByHangerCodeIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recommend(request("UNKNOWN")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.HANGER_NOT_FOUND));
    }

    @Test
    @DisplayName("추천할 재고 상품이 없으면 AI를 호출하지 않는다")
    void noCandidates() {
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        when(productRepository.findByHangerCodeIgnoreCase("H-0001")).thenReturn(Optional.of(selected));
        when(productRepository.findAvailableForStyling(eq(1L), any(Pageable.class))).thenReturn(List.of());

        assertThatThrownBy(() -> service.recommend(request("H-0001")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.STYLING_CANDIDATE_NOT_FOUND));
    }

    @Test
    @DisplayName("저장된 코디를 ID로 조회한다")
    void findSavedStyling() {
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        Product shirt = product(2L, "로고 티셔츠", "TSHIRT_TOP", "White", "H-0002");
        StylingRecommendation styling = new StylingRecommendation(
                selected,
                "데이트",
                "미니멀",
                List.of("검정", "흰색"),
                "모던 모노크롬 룩",
                "화이트 상의로 대비를 주세요.",
                "/api/styling/recommendations/10/image"
        );
        styling.addItem(shirt, "검정 백팩과 선명한 대비를 만듭니다.", 1);
        ReflectionTestUtils.setField(styling, "id", 10L);

        when(stylingRecommendationRepository.findById(10L)).thenReturn(Optional.of(styling));

        StylingRecommendationResponse response = service.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.lookName()).isEqualTo("모던 모노크롬 룩");
        assertThat(response.kodi()).isEqualTo("/api/styling/recommendations/10/image");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).product().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("저장되지 않은 코디 ID는 STYLING_NOT_FOUND 오류를 반환한다")
    void stylingNotFound() {
        when(stylingRecommendationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STYLING_NOT_FOUND));
    }

    @Test
    @DisplayName("스타일별 최신 코디를 제한 개수만큼 조회한다")
    void findLatestStylingByMood() {
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        StylingRecommendation styling = new StylingRecommendation(
                selected,
                "데일리",
                "MINIMAL",
                List.of("검정"),
                "미니멀 룩",
                "색을 단순하게 맞추세요.",
                "/api/styling/recommendations/20/image"
        );
        ReflectionTestUtils.setField(styling, "id", 20L);
        when(stylingRecommendationRepository.findByMoodIgnoreCaseOrderByIdDesc(
                eq("MINIMAL"), any(Pageable.class)
        )).thenReturn(List.of(styling));

        List<StylingRecommendationResponse> responses = service.findLatest(" MINIMAL ", 4);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(20L);
        assertThat(responses.get(0).kodi()).isEqualTo("/api/styling/recommendations/20/image");
    }

    @Test
    @DisplayName("저장하기를 누르면 코디를 최종 선택 상태로 변경한다")
    void selectStyling() {
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        StylingRecommendation styling = new StylingRecommendation(
                selected,
                "데이트",
                "미니멀",
                List.of("검정", "흰색"),
                "모던 모노크롬 룩",
                "화이트 상의로 대비를 주세요.",
                "/api/styling/recommendations/10/image"
        );
        ReflectionTestUtils.setField(styling, "id", 10L);
        when(stylingRecommendationRepository.findById(10L)).thenReturn(Optional.of(styling));

        StylingRecommendationResponse response = service.select(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.kodiSelected())
                .isEqualTo("/api/styling/recommendations/10/image");
        assertThat(styling.getKodiSelected())
                .isEqualTo("/api/styling/recommendations/10/image");
        verify(stylingRecommendationRepository).flush();
    }

    @Test
    @DisplayName("Aiven DB에 저장된 코디 이미지 바이트를 조회한다")
    void findStoredImage() {
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        StylingRecommendation styling = new StylingRecommendation(
                selected,
                "데이트",
                "미니멀",
                List.of("검정"),
                "모던 룩",
                "선택 상품을 유지합니다.",
                ""
        );
        ReflectionTestUtils.setField(styling, "id", 10L);
        styling.attachGeneratedImage(new byte[]{1, 2, 3});
        when(stylingRecommendationRepository.findById(10L)).thenReturn(Optional.of(styling));

        StylingRecommendationService.StoredStylingImage image = service.findImage(10L);

        assertThat(image.bytes()).containsExactly(1, 2, 3);
        assertThat(image.contentType()).isEqualTo("image/png");
    }

    private StylingRecommendationRequest request(String hangerCode) {
        return new StylingRecommendationRequest(hangerCode, "데이트", "미니멀", List.of("검정", "흰색"));
    }

    private Product product(Long id, String name, String category, String color, String hangerCode) {
        Product product = new Product(
                name, category, color, "ONE SIZE", 100000,
                "테스트 상품 설명", "https://example.com/product.jpg", 10, hangerCode
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
