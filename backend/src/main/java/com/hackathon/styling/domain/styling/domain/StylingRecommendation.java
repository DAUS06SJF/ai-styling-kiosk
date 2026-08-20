package com.hackathon.styling.domain.styling.domain;

import com.hackathon.styling.domain.admin.domain.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "styling_recommendations")
public class StylingRecommendation {

    private static final String COLOR_SEPARATOR = "|";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_product_id", nullable = false)
    private Product selectedProduct;

    @Column(length = 100)
    private String occasion;

    @Column(length = 100)
    private String mood;

    @Column(name = "preferred_colors", length = 500)
    private String preferredColors;

    @Column(name = "look_name", nullable = false, length = 200)
    private String lookName;

    @Column(name = "styling_tip", nullable = false, columnDefinition = "TEXT")
    private String stylingTip;

    @OneToMany(mappedBy = "stylingRecommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private final List<StylingRecommendationItem> items = new ArrayList<>();

    // AI가 생성한 코디 이미지에 접근할 수 있는 URL
    @Column(name = "kodi", nullable = false, length = 1000)
    private String kodi;

    // 사용자가 저장하기 버튼으로 최종 선택한 코디 이미지 URL
    @Column(name = "kodi_selected", length = 1000)
    private String kodiSelected;

    // Render의 로컬 디스크는 재배포 시 초기화되므로 생성 이미지는 DB에도 보관한다.
    @Lob
    @Column(name = "kodi_image", columnDefinition = "LONGBLOB")
    private byte[] kodiImage;

    @Column(name = "kodi_image_content_type", length = 100)
    private String kodiImageContentType;

    public StylingRecommendation(
            Product selectedProduct,
            String occasion,
            String mood,
            List<String> preferredColors,
            String lookName,
            String stylingTip,
            String kodi
    ) {
        this.selectedProduct = selectedProduct;
        this.occasion = occasion;
        this.mood = mood;
        this.preferredColors = joinColors(preferredColors);
        this.lookName = lookName;
        this.stylingTip = stylingTip;
        this.kodi = kodi;
    }

    public void addItem(Product product, String reason, int displayOrder) {
        items.add(new StylingRecommendationItem(this, product, reason, displayOrder));
    }

    public void select() {
        kodiSelected = kodi;
    }

    public void select(String imageUrl) {
        kodi = imageUrl;
        kodiSelected = imageUrl;
    }

    public void attachGeneratedImage(byte[] imageBytes) {
        if (id == null) {
            throw new IllegalStateException("코디를 저장한 뒤 이미지를 연결해야 합니다.");
        }
        kodiImage = imageBytes.clone();
        kodiImageContentType = "image/png";
        kodi = "/api/styling/recommendations/" + id + "/image";
    }

    public List<String> getPreferredColorList() {
        if (preferredColors == null || preferredColors.isBlank()) {
            return List.of();
        }
        return Arrays.stream(preferredColors.split("\\|"))
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String joinColors(List<String> colors) {
        if (colors == null || colors.isEmpty()) {
            return "";
        }
        return colors.stream()
                .filter(color -> color != null && !color.isBlank())
                .map(String::trim)
                .map(color -> color.replace(COLOR_SEPARATOR, ""))
                .reduce((left, right) -> left + COLOR_SEPARATOR + right)
                .orElse("");
    }
}
