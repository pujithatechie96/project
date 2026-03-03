package com.diaperbazaar.project.repository;


import com.diaperbazaar.project.dto.LowStockVariantDTO;
import com.diaperbazaar.project.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<ProductVariant, Long> {

    @Query(value = """
            SELECT
                c.name    AS category_name,
                c.slug    AS category_slug,
                pv.title  AS variant_title,
                b.name    AS brand,
                pv.id     AS variant_id,
                pv.stock
            FROM product_variants pv
            JOIN products p   ON p.id = pv.product_id
            JOIN categories c ON c.id = p.category_id
            JOIN brands b     ON b.id = p.brand_id
            WHERE pv.stock <= :threshold;
        """, nativeQuery = true)
    List<Object> findLowStockVariants(int threshold);
}
