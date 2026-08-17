package com.hackathon.styling.domain.styling.service;

import com.hackathon.styling.domain.admin.domain.Product;
import com.hackathon.styling.domain.admin.repository.ProductRepository;
import com.hackathon.styling.domain.styling.client.StylingAiClient;
import com.hackathon.styling.domain.styling.client.StylingAiInput;
import com.hackathon.styling.domain.styling.client.StylingAiOutput;
import com.hackathon.styling.domain.styling.client.StylingImageClient;
import com.hackathon.styling.domain.styling.config.OpenAiProperties;
import com.hackathon.styling.domain.styling.domain.StylingRecommendation;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationRequest;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import com.hackathon.styling.domain.styling.repository.StylingRecommendationRepository;
import com.hackathon.styling.domain.styling.storage.StylingImageStorage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private StylingImageStorage stylingImageStorage;

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
                stylingImageStorage,
                properties
        );
    }

    @Test
    @DisplayName("행거 상품과 재고 후보를 이용해 AI 코디를 생성한다")
    void recommendStyling() {
        Product selected = product(1L, "Aren 백팩", "BACKPACK", "Black", "H-0001");
        Product shirt = product(2L, "로고 티셔츠", "TSHIRT_TOP", "White", "H-0002");
        Product shoes = product(3L, "레더 스니커즈", "SHOES", "Black", "H-0003");

        when(productRepository.findByHangerCodeIgnoreCase("H-0001")).thenReturn(Optional.of(selected));
        when(productRepository.findAvailableForStyling(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(shirt, shoes));
        when(stylingAiClient.generate(any(StylingAiInput.class))).thenReturn(new StylingAiOutput(
                "모던 모노크롬 룩",
                "화이트 상의로 대비를 주세요.",
                List.of(
                        new StylingAiOutput.Recommendation(2L, "검정 백팩과 선명한 대비를 만듭니다."),
                        new StylingAiOutput.Recommendation(999L, "존재하지 않는 상품"),
                        new StylingAiOutput.Recommendation(2L, "중복 추천")
                )
        ));
        when(stylingImageClient.generate(any())).thenReturn(new byte[]{1, 2, 3});
        when(stylingImageStorage.store(any(byte[].class)))
                .thenReturn("http://localhost:8080/generated-stylings/test.png");

        StylingRecommendationResponse response = service.recommend(request(" H-0001 "));

        assertThat(response.lookName()).isEqualTo("모던 모노크롬 룩");
        assertThat(response.selectedProduct().getId()).isEqualTo(1L);
        assertThat(response.occasion()).isEqualTo("데이트");
        assertThat(response.preferredColors()).containsExactly("검정", "흰색");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).product().getId()).isEqualTo(2L);
        assertThat(response.kodi()).isEqualTo("http://localhost:8080/generated-stylings/test.png");
        assertThat(response.kodiSelected()).isNull();
        verify(stylingRecommendationRepository).save(any(StylingRecommendation.class));

        ArgumentCaptor<StylingAiInput> captor = ArgumentCaptor.forClass(StylingAiInput.class);
        verify(stylingAiClient).generate(captor.capture());
        assertThat(captor.getValue().candidateProducts()).extracting(StylingAiInput.AiProduct::productId)
                .containsExactly(2L, 3L);
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
                "http://localhost:8080/generated-stylings/saved.png"
        );
        styling.addItem(shirt, "검정 백팩과 선명한 대비를 만듭니다.", 1);
        ReflectionTestUtils.setField(styling, "id", 10L);

        when(stylingRecommendationRepository.findById(10L)).thenReturn(Optional.of(styling));

        StylingRecommendationResponse response = service.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.lookName()).isEqualTo("모던 모노크롬 룩");
        assertThat(response.kodi()).endsWith("saved.png");
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
                "http://localhost:8080/generated-stylings/minimal.png"
        );
        ReflectionTestUtils.setField(styling, "id", 20L);
        when(stylingRecommendationRepository.findByMoodIgnoreCaseOrderByIdDesc(
                eq("MINIMAL"), any(Pageable.class)
        )).thenReturn(List.of(styling));

        List<StylingRecommendationResponse> responses = service.findLatest(" MINIMAL ", 4);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(20L);
        assertThat(responses.get(0).kodi()).endsWith("minimal.png");
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
                "http://localhost:8080/generated-stylings/saved.png"
        );
        ReflectionTestUtils.setField(styling, "id", 10L);
        when(stylingRecommendationRepository.findById(10L)).thenReturn(Optional.of(styling));

        StylingRecommendationResponse response = service.select(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.kodiSelected())
                .isEqualTo("http://localhost:8080/generated-stylings/saved.png");
        assertThat(styling.getKodiSelected())
                .isEqualTo("http://localhost:8080/generated-stylings/saved.png");
        verify(stylingRecommendationRepository).flush();
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
