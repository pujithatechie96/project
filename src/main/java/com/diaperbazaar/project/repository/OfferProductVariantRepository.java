package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.OfferProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for OfferProductVariant entity operations
 */
@Repository
public interface OfferProductVariantRepository extends JpaRepository<OfferProductVariant, Long> {

    /**
     * Find all mappings for an offer
     */
    List<OfferProductVariant> findByOfferId(Long offerId);

    /**
     * Find all mappings for a product
     */
    List<OfferProductVariant> findByProductId(Long productId);

    /**
     * Find all mappings for a product variant
     */
    List<OfferProductVariant> findByProductVariantId(Long productVariantId);

    /**
     * Check if mapping exists
     */
    boolean existsByOfferIdAndProductIdAndProductVariantId(Long offerId, Long productId, Long productVariantId);

    /**
     * Delete all mappings for an offer
     */
    @Modifying
    @Transactional
    void deleteByOfferId(Long offerId);

    /**
     * Delete specific mapping
     */
    @Modifying
    @Transactional
    void deleteByOfferIdAndProductIdAndProductVariantId(Long offerId, Long productId, Long productVariantId);

    /**
     * Count variants mapped to an offer
     */
    long countByOfferId(Long offerId);

    /**
     * Get all variant IDs for an offer
     */
    @Query("SELECT opv.productVariantId FROM OfferProductVariant opv WHERE opv.offer.id = :offerId")
    List<Long> findVariantIdsByOfferId(@Param("offerId") Long offerId);

    /**
     * Get all product IDs for an offer
     */
    @Query("SELECT DISTINCT opv.productId FROM OfferProductVariant opv WHERE opv.offer.id = :offerId")
    List<Long> findProductIdsByOfferId(@Param("offerId") Long offerId);
}
