package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Offer entity operations
 */
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    /**
     * Find all active offers within date range, ordered by priority
     */
    @Query("SELECT o FROM Offer o WHERE o.isActive = true " +
           "AND :now BETWEEN o.startDate AND o.endDate " +
           "ORDER BY o.priority DESC")
    List<Offer> findActiveOffers(@Param("now") LocalDateTime now);

    /**
     * Find active offers for a specific product variant
     */
    @Query("SELECT DISTINCT o FROM Offer o " +
           "JOIN o.productVariants opv " +
           "WHERE opv.productVariantId = :variantId " +
           "AND o.isActive = true " +
           "AND :now BETWEEN o.startDate AND o.endDate " +
           "ORDER BY o.priority DESC")
    List<Offer> findActiveOffersForVariant(@Param("variantId") Long variantId, 
                                            @Param("now") LocalDateTime now);

    /**
     * Find the highest priority active offer for a variant
     */
    @Query("SELECT o FROM Offer o " +
           "JOIN o.productVariants opv " +
           "WHERE opv.productVariantId = :variantId " +
           "AND o.isActive = true " +
           "AND :now BETWEEN o.startDate AND o.endDate " +
           "ORDER BY o.priority DESC")
    List<Offer> findBestOfferForVariant(@Param("variantId") Long variantId, 
                                         @Param("now") LocalDateTime now);

    /**
     * Find offers by type
     */
    List<Offer> findByOfferTypeAndIsActiveTrue(Offer.OfferType offerType);

    /**
     * Find all offers with pagination
     */
    Page<Offer> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find active offers with pagination
     */
    Page<Offer> findByIsActiveTrueOrderByPriorityDesc(Pageable pageable);

    /**
     * Count active offers
     */
    long countByIsActiveTrue();

    /**
     * Find expired offers
     */
    @Query("SELECT o FROM Offer o WHERE o.endDate < :now AND o.isActive = true")
    List<Offer> findExpiredActiveOffers(@Param("now") LocalDateTime now);

    /**
     * Search offers by name
     */
    @Query("SELECT o FROM Offer o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Offer> searchByName(@Param("search") String search, Pageable pageable);
}
