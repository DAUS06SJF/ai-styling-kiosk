package com.hackathon.styling.domain.styling.repository;

import com.hackathon.styling.domain.styling.domain.StylingRecommendation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StylingRecommendationRepository extends JpaRepository<StylingRecommendation, Long> {

    @Override
    @EntityGraph(attributePaths = {"selectedProduct", "items", "items.product"})
    Optional<StylingRecommendation> findById(Long id);
}
