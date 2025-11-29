package com.diaperbazaar.project.repository;


import com.diaperbazaar.project.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b " +
            "WHERE (:keyword IS NULL OR " +
            "   LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR LOWER(c.slug) = LOWER(:category)) " +
            "AND (:brandId IS NULL OR b.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<com.diaperbazaar.project.entity.Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("brandId") Long brandId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p " +
            "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<com.diaperbazaar.project.entity.Product> findAllWithFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "WHERE LOWER(c.slug) = LOWER(:categorySlug) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<com.diaperbazaar.project.entity.Product> findByCategorySlugWithFilters(
            @Param("categorySlug") String categorySlug,
            @Param("brandId") Long brandId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "WHERE p.brand.id = :brandId " +
            "AND (:category IS NULL OR LOWER(c.slug) = LOWER(:category)) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByBrandWithFilters(
            @Param("brandId") Long brandId,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    Optional<Product> findBySlug(String slug);

    @Query("SELECT DISTINCT ps.size FROM ProductSize ps ORDER BY ps.size")
    List<String> findDistinctSizes();

    @Query("SELECT DISTINCT ps.size FROM ProductSize ps JOIN ps.product p WHERE p.category.slug = :categorySlug ORDER BY ps.size")
    List<String> findDistinctSizesByCategorySlug(@Param("categorySlug") String categorySlug);

}