package com.hackathon.styling.domain.styling.domain;

import com.hackathon.styling.domain.admin.domain.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    // 사용자가 저장하기 버튼으로 최종 선택한 코디인지 여부
    @Column(name = "kodi_selected", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean kodiSelected = false;

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
        kodiSelected = true;
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
