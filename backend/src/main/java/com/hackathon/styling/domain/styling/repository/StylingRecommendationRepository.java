package com.hackathon.styling.domain.styling.repository;

import com.hackathon.styling.domain.styling.domain.StylingRecommendation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StylingRecommendationRepository extends JpaRepository<StylingRecommendation, Long> {

    @Override
    @EntityGraph(attributePaths = {"selectedProduct", "items", "items.product"})
    Optional<StylingRecommendation> findById(Long id);

    @EntityGraph(attributePaths = {"selectedProduct", "items", "items.product"})
    List<StylingRecommendation> findByMoodIgnoreCaseOrderByIdDesc(String mood, Pageable pageable);
}
