package com.hackathon.styling.domain.styling.domain;

import com.hackathon.styling.domain.admin.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "styling_recommendation_items")
public class StylingRecommendationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "styling_recommendation_id", nullable = false)
    private StylingRecommendation stylingRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    StylingRecommendationItem(
            StylingRecommendation stylingRecommendation,
            Product product,
            String reason,
            int displayOrder
    ) {
        this.stylingRecommendation = stylingRecommendation;
        this.product = product;
        this.reason = reason;
        this.displayOrder = displayOrder;
    }
}
