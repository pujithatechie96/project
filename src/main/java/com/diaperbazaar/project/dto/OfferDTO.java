package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.Offer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Offer entity - used for API requests/responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDTO {

    private Long id;
    private String name;
    private String description;
    private Offer.OfferType offerType;

    // BUY_X_GET_Y fields
    private Integer buyQty;
    private Integer getQty;

    // BUNDLE_PRICE fields
    private Integer bundleQty;
    private BigDecimal bundlePrice;

    // FLAT_DISCOUNT & PERCENT_DISCOUNT fields
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;

    // PRICE_OVERRIDE fields
    private BigDecimal overridePrice;

    // FREE_ITEM fields
    private Long freeVariantId;
    private Integer freeItemQty;

    // Validity and status
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer priority;

    // Constraints
    private Integer minOrderQty;
    private Integer maxUsagePerOrder;

    // Mapped product and variant IDs
    private List<Long> productIds;
    private List<Long> productVariantIds;
    private List<ProductVariantMapping> productVariantMappings;

    // Computed fields
    private String status; // ACTIVE, SCHEDULED, EXPIRED, INACTIVE
    private Long productCount;

    /**
     * Inner class for product-variant mapping display
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductVariantMapping {
        private Long productId;
        private String productName;
        private Long variantId;
        private String variantTitle;
        private String variantSize;
    }

    /**
     * Convert entity to DTO
     */
    public static OfferDTO fromEntity(Offer offer) {
        return OfferDTO.builder()
                .id(offer.getId())
                .name(offer.getName())
                .description(offer.getDescription())
                .offerType(offer.getOfferType())
                .buyQty(offer.getBuyQty())
                .getQty(offer.getGetQty())
                .bundleQty(offer.getBundleQty())
                .bundlePrice(offer.getBundlePrice())
                .discountValue(offer.getDiscountValue())
                .maxDiscount(offer.getMaxDiscount())
                .overridePrice(offer.getOverridePrice())
                .freeVariantId(offer.getFreeVariantId())
                .freeItemQty(offer.getFreeItemQty())
                .isActive(offer.getIsActive())
                .startDate(offer.getStartDate())
                .endDate(offer.getEndDate())
                .priority(offer.getPriority())
                .minOrderQty(offer.getMinOrderQty())
                .maxUsagePerOrder(offer.getMaxUsagePerOrder())
                .status(calculateStatus(offer))
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public Offer toEntity() {
        return Offer.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .offerType(this.offerType)
                .buyQty(this.buyQty)
                .getQty(this.getQty)
                .bundleQty(this.bundleQty)
                .bundlePrice(this.bundlePrice)
                .discountValue(this.discountValue)
                .maxDiscount(this.maxDiscount)
                .overridePrice(this.overridePrice)
                .freeVariantId(this.freeVariantId)
                .freeItemQty(this.freeItemQty)
                .isActive(this.isActive)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .priority(this.priority)
                .minOrderQty(this.minOrderQty)
                .maxUsagePerOrder(this.maxUsagePerOrder)
                .build();
    }

    private static String calculateStatus(Offer offer) {
        LocalDateTime now = LocalDateTime.now();
        if (offer.getIsActive() == null || !offer.getIsActive()) {
            return "INACTIVE";
        }
        if (now.isBefore(offer.getStartDate())) {
            return "SCHEDULED";
        }
        if (now.isAfter(offer.getEndDate())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
