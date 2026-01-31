package com.diaperbazaar.project.strategy.impl;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.strategy.OfferStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * FreeItemStrategy - Implements free product/variant offers
 * Example: Buy any product, get a free sample
 * 
 * Adds a different product variant as a free item to the order
 */
@Component
public class FreeItemStrategy implements OfferStrategy {

    @Override
    public OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice) {
        Long freeVariantId = offer.getFreeVariantId();
        int freeItemQty = offer.getFreeItemQty() != null ? offer.getFreeItemQty() : 1;
        int minOrderQty = offer.getMinOrderQty() != null ? offer.getMinOrderQty() : 1;

        // Validate offer configuration
        if (freeVariantId == null || orderedQty < minOrderQty) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(orderedQty));

        // Calculate how many free items based on order quantity
        int totalFreeItems = freeItemQty;
        if (offer.getMaxUsagePerOrder() != null) {
            int applicableTimes = orderedQty / minOrderQty;
            applicableTimes = Math.min(applicableTimes, offer.getMaxUsagePerOrder());
            totalFreeItems = freeItemQty * applicableTimes;
        }

        // Create free item list
        List<OfferResult.FreeItem> freeItems = new ArrayList<>();
        freeItems.add(OfferResult.FreeItem.builder()
                .productVariantId(freeVariantId)
                .quantity(totalFreeItems)
                .build());

        String description = String.format("Free item: %d x variant #%d included", 
                totalFreeItems, freeVariantId);

        return OfferResult.builder()
                .billableQty(orderedQty)
                .deliveredQty(orderedQty) // Free items are separate, not added to deliveredQty
                .subtotal(subtotal)
                .originalSubtotal(subtotal)
                .discountAmount(BigDecimal.ZERO) // No discount on price, free item is separate
                .appliedOfferName(offer.getName())
                .appliedOfferId(offer.getId())
                .offerType(offer.getOfferType().name())
                .offerApplied(true)
                .offerDescription(description)
                .freeItems(freeItems)
                .build();
    }

    @Override
    public boolean supports(Offer.OfferType offerType) {
        return Offer.OfferType.FREE_ITEM.equals(offerType);
    }

    @Override
    public Offer.OfferType getOfferType() {
        return Offer.OfferType.FREE_ITEM;
    }
}
