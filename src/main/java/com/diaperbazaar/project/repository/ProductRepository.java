package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Product;
import com.diaperbazaar.project.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
SELECT DISTINCT p
FROM Product p
JOIN p.variants pv
LEFT JOIN p.category c
LEFT JOIN p.brand b
WHERE (:keyword IS NULL OR
       LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
       LOWER(pv.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
AND (:categories IS NULL OR c.slug IN :categories)
AND (:brandIds IS NULL OR b.id IN :brandIds)
AND (:productSize IS NULL OR pv.size = :productSize)
AND (:minPrice IS NULL OR pv.sellPrice >= :minPrice)
AND (:maxPrice IS NULL OR pv.sellPrice <= :maxPrice)
AND pv.stock > 0
""")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categories") List<String> categories,
            @Param("brandIds") List<Long> brandIds,
            @Param("productSize") String productSize,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    /* ================= ALL PRODUCTS ================= */

    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN p.variants pv
        WHERE (:minPrice IS NULL OR pv.sellPrice >= :minPrice)
        AND (:maxPrice IS NULL OR pv.sellPrice <= :maxPrice)
        AND pv.stock > 0
        """)
    Page<Product> findAllWithFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    /* ================= CATEGORY FILTER ================= */

    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN p.variants pv
        JOIN p.category c
        LEFT JOIN p.brand b
        WHERE LOWER(c.slug) = LOWER(:categorySlug)
        AND (:brandId IS NULL OR b.id = :brandId)
        AND (:size IS NULL OR pv.size = :size)
        AND (:productType IS NULL OR p.productType = :productType)
        AND (:wearType IS NULL OR pv.wearType = :wearType)
        AND (:minPrice IS NULL OR pv.sellPrice >= :minPrice)
        AND (:maxPrice IS NULL OR pv.sellPrice <= :maxPrice)
        AND pv.stock > 0
        """)
    Page<Product> findByCategorySlugWithFilters(
            @Param("categorySlug") String categorySlug,
            @Param("brandId") Long brandId,
            @Param("size") String size,
            @Param("productType") String productType,
            @Param("wearType") String wearType,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    /* ================= BRAND FILTER ================= */

    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN p.variants pv
        LEFT JOIN p.category c
        WHERE p.brand.id = :brandId
        AND (:category IS NULL OR LOWER(c.slug) = LOWER(:category))
        AND (:minPrice IS NULL OR pv.sellPrice >= :minPrice)
        AND (:maxPrice IS NULL OR pv.sellPrice <= :maxPrice)
        AND pv.stock > 0
        """)
    Page<Product> findByBrandWithFilters(
            @Param("brandId") Long brandId,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    /* ================= SINGLE PRODUCT ================= */

    Product findBySlug(String slug);

    boolean existsBySlug(String slug);

    /* ================= SIZE FILTER DATA ================= */
    /* (size now comes from ProductVariant) */

    @Query("""
        SELECT DISTINCT pv.size
        FROM ProductVariant pv
        ORDER BY pv.size
        """)
    List<String> findDistinctSizes();

    @Query("""
        SELECT DISTINCT pv.size
        FROM ProductVariant pv
        JOIN pv.product p
        WHERE p.category.slug IN :categorySlug
        ORDER BY pv.size
        """)
    List<String> findDistinctSizesByCategorySlug(
            @Param("categorySlug") List<String> categorySlug
    );

        @Query("""
    SELECT DISTINCT pv.size
    FROM ProductVariant pv
    JOIN pv.product p
    JOIN p.brand b
    WHERE b.slug = :brandName
    ORDER BY pv.size
    """)
    List<String> findSizesByBrandName(@Param("brandName") String brandName);

    @Query("""
SELECT DISTINCT pv.size
FROM ProductVariant pv
JOIN pv.product p
JOIN p.brand b
JOIN p.category c
WHERE b.slug IN :brandSlugs
AND c.slug IN :categorySlugs
ORDER BY pv.size
""")
    List<String> findSizesByBrandsAndCategories(
            @Param("brandSlugs") List<String> brandSlugs,
            @Param("categorySlugs") List<String> categorySlugs
    );

}
