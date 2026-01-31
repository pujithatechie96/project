package com.diaperbazaar.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * OfferResult - Result of applying an offer strategy
 * Contains calculated quantities, pricing, and any free items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResult {

    /**
     * Quantity that customer pays for
     */
    private int billableQty;

    /**
     * Total quantity delivered (including free items)
     */
    private int deliveredQty;

    /**
     * Subtotal before GST (after discount applied)
     */
    private BigDecimal subtotal;

    /**
     * Original subtotal before any discount
     */
    private BigDecimal originalSubtotal;

    /**
     * Discount amount applied
     */
    private BigDecimal discountAmount;

    /**
     * Name of the applied offer
     */
    private String appliedOfferName;

    /**
     * ID of the applied offer
     */
    private Long appliedOfferId;

    /**
     * Type of offer applied
     */
    private String offerType;

    /**
     * Free items to be added to order (for FREE_ITEM offer type)
     */
    @Builder.Default
    private List<FreeItem> freeItems = new ArrayList<>();

    /**
     * Whether an offer was actually applied
     */
    private boolean offerApplied;

    /**
     * Description of how the offer was applied
     */
    private String offerDescription;

    /**
     * Inner class representing a free item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FreeItem {
        private Long productVariantId;
        private int quantity;
        private String variantTitle;
    }

    /**
     * Create a result with no offer applied
     */
    public static OfferResult noOffer(int quantity, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return OfferResult.builder()
                .billableQty(quantity)
                .deliveredQty(quantity)
                .subtotal(subtotal)
                .originalSubtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .offerApplied(false)
                .freeItems(new ArrayList<>())
                .build();
    }
}
