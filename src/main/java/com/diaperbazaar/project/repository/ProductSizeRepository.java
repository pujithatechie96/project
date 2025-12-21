package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.ProductSize;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    List<ProductSize> findByProductId(Long productId);

        @Query("""
    SELECT ps FROM ProductSize ps
    WHERE ps.product.id = :productId AND ps.isDefault = true
    """)
        Optional<ProductSize> findDefaultSize(Long productId);


    Optional<ProductSize> findByProductIdAndSize(@NotNull(message = "Product ID is required") Long productId, String size);

    @Modifying
    @Transactional
    @Query("""
        update ProductSize ps
        set ps.stock = ps.stock - :qty
        where ps.product.id = :productId
          and ps.size = :size
          and ps.stock >= :qty
    """)
    int decreaseStock(
            @Param("productId") Long productId,
            @Param("size") String size,
            @Param("qty") int qty
    );

}
