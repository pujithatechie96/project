package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.ProductVariant;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    @Query("""
        SELECT pv FROM ProductVariant pv
        WHERE pv.product.id = :productId
        AND pv.isDefault = true
        """)
    Optional<ProductVariant> findDefaultVariant(Long productId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ProductVariant pv
        SET pv.stock = pv.stock - :qty
        WHERE pv.id = :variantId
        AND pv.stock >= :qty
        """)
    int decreaseStock(Long variantId, int qty);


    void deleteByProductId(Long id);
}
