package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /* ---------- CHECK ---------- */
    boolean existsByUserIdAndVariantId(Long userId, Long variantId);

    /* ---------- DELETE ---------- */
    void deleteByUserIdAndVariantId(Long userId, Long variantId);

    /* ---------- FETCH WISHLIST ---------- */
    @Query("""
        SELECT w FROM Wishlist w
        JOIN FETCH w.product p
        JOIN FETCH w.variant v
        WHERE w.userId = :userId
    """)
    List<Wishlist> findByUserIdWithProductAndVariant(
            @Param("userId") Long userId
    );
}
