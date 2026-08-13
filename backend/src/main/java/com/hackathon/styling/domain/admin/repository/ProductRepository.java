package com.hackathon.styling.domain.admin.repository;

import com.hackathon.styling.domain.admin.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByHangerCodeIgnoreCase(String hangerCode);

    @Query("""
            SELECT p FROM Product p
            WHERE p.stock > 0
              AND p.id <> :excludedProductId
            ORDER BY p.stock DESC, p.id ASC
            """)
    List<Product> findAvailableForStyling(
            @Param("excludedProductId") Long excludedProductId,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.hangerCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            """)
    Page<Product> search(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable
    );
}
