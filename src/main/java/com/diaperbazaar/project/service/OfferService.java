package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.entity.OfferProductVariant;
import com.diaperbazaar.project.entity.ProductVariant;
import com.diaperbazaar.project.repository.OfferProductVariantRepository;
import com.diaperbazaar.project.repository.OfferRepository;
import com.diaperbazaar.project.repository.ProductVariantRepository;
import com.diaperbazaar.project.strategy.OfferStrategy;
import com.diaperbazaar.project.strategy.OfferStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OfferService - Business logic for offer management and application
 * Handles CRUD operations and offer calculation using Strategy Pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferProductVariantRepository offerProductVariantRepository;
    private final OfferStrategyFactory strategyFactory;
    private final ProductVariantRepository productVariantRepository;

    // =====================================================
    // OFFER CRUD OPERATIONS
    // =====================================================

    /**
     * Create a new offer
     */
    public Offer createOffer(Offer offer) {
        validateOffer(offer);
        return offerRepository.save(offer);
    }

    /**
     * Update an existing offer
     */
    public Offer updateOffer(Long id, Offer offerDetails) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));

        offer.setName(offerDetails.getName());
        offer.setDescription(offerDetails.getDescription());
        offer.setOfferType(offerDetails.getOfferType());
        offer.setBuyQty(offerDetails.getBuyQty());
        offer.setGetQty(offerDetails.getGetQty());
        offer.setBundleQty(offerDetails.getBundleQty());
        offer.setBundlePrice(offerDetails.getBundlePrice());
        offer.setDiscountValue(offerDetails.getDiscountValue());
        offer.setMaxDiscount(offerDetails.getMaxDiscount());
        offer.setOverridePrice(offerDetails.getOverridePrice());
        offer.setFreeVariantId(offerDetails.getFreeVariantId());
        offer.setFreeItemQty(offerDetails.getFreeItemQty());
        offer.setIsActive(offerDetails.getIsActive());
        offer.setStartDate(offerDetails.getStartDate());
        offer.setEndDate(offerDetails.getEndDate());
        offer.setPriority(offerDetails.getPriority());
        offer.setMinOrderQty(offerDetails.getMinOrderQty());
        offer.setMaxUsagePerOrder(offerDetails.getMaxUsagePerOrder());

        return offerRepository.save(offer);
    }

    /**
     * Delete an offer
     */
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found with id: " + id);
        }
        offerRepository.deleteById(id);
    }

    /**
     * Get offer by ID
     */
    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    /**
     * Get all offers with pagination
     */
    public Page<Offer> getAllOffers(Pageable pageable) {
        return offerRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * Search offers by name
     */
    public Page<Offer> searchOffers(String search, Pageable pageable) {
        return offerRepository.searchByName(search, pageable);
    }

    /**
     * Toggle offer active status
     */
    public Offer toggleOfferStatus(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
        offer.setIsActive(!offer.getIsActive());
        return offerRepository.save(offer);
    }

    // =====================================================
    // PRODUCT VARIANT MAPPING OPERATIONS
    // =====================================================

    /**
     * Add product variants to an offer with product ID
     */
    public void addVariantsToOffer(Long offerId, List<ProductVariantMappingInput> mappings) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        for (ProductVariantMappingInput mapping : mappings) {
            if (!offerProductVariantRepository.existsByOfferIdAndProductIdAndProductVariantId(
                    offerId, mapping.getProductId(), mapping.getVariantId())) {
                OfferProductVariant opv = OfferProductVariant.builder()
                        .offer(offer)
                        .productId(mapping.getProductId())
                        .productVariantId(mapping.getVariantId())
                        .build();
                offerProductVariantRepository.save(opv);
            }
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductVariantMappingInput {
        private Long productId;
        private Long variantId;
    }

    /**
     * Remove a product variant from an offer
     */
    public void removeVariantFromOffer(Long offerId, Long productId, Long variantId) {
        offerProductVariantRepository.deleteByOfferIdAndProductIdAndProductVariantId(offerId, productId, variantId);
    }

    /**
     * Replace all variants for an offer
     */
    public void replaceVariantsForOffer(Long offerId, List<ProductVariantMappingInput> mappings) {
        offerProductVariantRepository.deleteByOfferId(offerId);
        addVariantsToOffer(offerId, mappings);
    }

    /**
     * Get all product-variant mappings for an offer
     */
    public List<OfferProductVariant> getMappingsForOffer(Long offerId) {
        return offerProductVariantRepository.findByOfferId(offerId);
    }

    /**
     * Get all variant IDs for an offer
     */
    public List<Long> getVariantIdsForOffer(Long offerId) {
        return offerProductVariantRepository.findVariantIdsByOfferId(offerId);
    }

    // =====================================================
    // OFFER APPLICATION LOGIC
    // =====================================================

    /**
     * Get the best applicable offer for a product variant
     * Returns the highest priority active offer within valid date range
     */
    public Optional<Offer> getBestOfferForVariant(Long variantId) {
        LocalDateTime now = LocalDateTime.now();
        List<Offer> offers = offerRepository.findBestOfferForVariant(variantId, now);
        return offers.isEmpty() ? Optional.empty() : Optional.of(offers.get(0));
    }

    /**
     * Get all active offers for a product variant
     */
    public List<Offer> getActiveOffersForVariant(Long variantId) {
        return offerRepository.findActiveOffersForVariant(variantId, LocalDateTime.now());
    }

    /**
     * Apply the best offer to a product variant
     * This is the main method used during order creation
     *
     * @param variantId   Product variant ID
     * @param orderedQty  Quantity ordered
     * @param unitPrice   Unit price of the variant
     * @return OfferResult with calculated quantities and pricing
     */
    public OfferResult applyBestOffer(Long variantId, int orderedQty, BigDecimal unitPrice) {
        Optional<Offer> bestOffer = getBestOfferForVariant(variantId);

        if (bestOffer.isEmpty()) {
            log.debug("No active offer found for variant {}", variantId);
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        Offer offer = bestOffer.get();

        // Check minimum order quantity
        if (offer.getMinOrderQty() != null && orderedQty < offer.getMinOrderQty()) {
            log.debug("Order quantity {} below minimum {} for offer {}",
                    orderedQty, offer.getMinOrderQty(), offer.getName());
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        try {
            OfferStrategy strategy = strategyFactory.getStrategy(offer);
            OfferResult result = strategy.apply(offer, orderedQty, unitPrice);
            log.info("Applied offer '{}' to variant {}: {} -> {}",
                    offer.getName(), variantId, result.getOriginalSubtotal(), result.getSubtotal());
            return result;
        } catch (Exception e) {
            log.error("Error applying offer {} to variant {}: {}",
                    offer.getId(), variantId, e.getMessage());
            return OfferResult.noOffer(orderedQty, unitPrice);
        }
    }

    /**
     * Calculate offer for preview (doesn't apply, just calculates)
     */
    public OfferResult previewOffer(Long offerId, int quantity, BigDecimal unitPrice) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        OfferStrategy strategy = strategyFactory.getStrategy(offer);
        return strategy.apply(offer, quantity, unitPrice);
    }

    /**
     * Preview cart item offer with GST calculation
     * Returns all pricing details including GST for frontend display
     */
    public CartItemOfferPreview previewCartItemOffer(Long variantId, int quantity, BigDecimal unitPrice) {
        OfferResult result = applyBestOffer(variantId, quantity, unitPrice);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // Calculate GST (5% default - can be configured per product)
        BigDecimal gstPercentage = variant.getGstPercentage() != null
                ? variant.getGstPercentage()
                : BigDecimal.ZERO;
        BigDecimal subtotalAfterDiscount = result.getSubtotal();
        BigDecimal gstAmount = subtotalAfterDiscount.multiply(gstPercentage).divide(new BigDecimal("100"));
        BigDecimal totalAmount = subtotalAfterDiscount.add(gstAmount);

        return CartItemOfferPreview.builder()
                .variantId(variantId)
                .billableQty(result.getBillableQty())
                .deliveredQty(result.getDeliveredQty())
                .subtotal(subtotalAfterDiscount)
                .discount(result.getDiscountAmount())
                .gstPercentage(gstPercentage)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .appliedOfferName(result.getAppliedOfferName())
                .appliedOfferDescription(result.getAppliedOfferName())
                .freeItems(result.getFreeItems())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CartItemOfferPreview {
        private Long variantId;
        private int billableQty;
        private int deliveredQty;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal gstPercentage;
        private BigDecimal gstAmount;
        private BigDecimal totalAmount;
        private String appliedOfferName;
        private String appliedOfferDescription;
        private List<OfferResult.FreeItem> freeItems;
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    /**
     * Validate offer configuration
     */
    private void validateOffer(Offer offer) {
        if (offer.getName() == null || offer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Offer name is required");
        }
        if (offer.getOfferType() == null) {
            throw new IllegalArgumentException("Offer type is required");
        }
        if (offer.getStartDate() == null || offer.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (offer.getStartDate().isAfter(offer.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Type-specific validations
        switch (offer.getOfferType()) {
            case BUY_X_GET_Y:
                if (offer.getBuyQty() == null || offer.getBuyQty() < 1) {
                    throw new IllegalArgumentException("Buy quantity must be at least 1 for BUY_X_GET_Y");
                }
                if (offer.getGetQty() == null || offer.getGetQty() < 1) {
                    throw new IllegalArgumentException("Get quantity must be at least 1 for BUY_X_GET_Y");
                }
                break;
            case BUNDLE_PRICE:
                if (offer.getBundleQty() == null || offer.getBundleQty() < 2) {
                    throw new IllegalArgumentException("Bundle quantity must be at least 2 for BUNDLE_PRICE");
                }
                if (offer.getBundlePrice() == null || offer.getBundlePrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Bundle price must be positive for BUNDLE_PRICE");
                }
                break;
            case FLAT_DISCOUNT:
            case PERCENT_DISCOUNT:
                if (offer.getDiscountValue() == null || offer.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Discount value must be positive");
                }
                break;
            case PRICE_OVERRIDE:
                if (offer.getOverridePrice() == null || offer.getOverridePrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Override price must be positive for PRICE_OVERRIDE");
                }
                break;
            case FREE_ITEM:
                if (offer.getFreeVariantId() == null) {
                    throw new IllegalArgumentException("Free variant ID is required for FREE_ITEM");
                }
                break;
        }
    }

    /**
     * Deactivate expired offers (can be called by scheduler)
     */
    public int deactivateExpiredOffers() {
        List<Offer> expiredOffers = offerRepository.findExpiredActiveOffers(LocalDateTime.now());
        for (Offer offer : expiredOffers) {
            offer.setIsActive(false);
            offerRepository.save(offer);
        }
        return expiredOffers.size();
    }

    /**
     * Get offer statistics
     */
    public OfferStats getOfferStats() {
        long totalOffers = offerRepository.count();
        long activeOffers = offerRepository.countByIsActiveTrue();
        return new OfferStats(totalOffers, activeOffers);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OfferStats {
        private long totalOffers;
        private long activeOffers;
    }
}
