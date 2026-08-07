package com.hackathon.styling.domain.admin.repository;

import com.hackathon.styling.domain.admin.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByHangerCode(String hangerCode);
}