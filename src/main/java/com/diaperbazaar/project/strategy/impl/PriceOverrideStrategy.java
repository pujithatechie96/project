package com.diaperbazaar.project.strategy.impl;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.strategy.OfferStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PriceOverrideStrategy - Implements fixed price override offers
 * Example: Special price ₹299 (replacing original MRP)
 * 
 * The override price replaces the unit price completely
 */
@Component
public class PriceOverrideStrategy implements OfferStrategy {

    @Override
    public OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice) {
        BigDecimal overridePrice = offer.getOverridePrice();
        int minOrderQty = offer.getMinOrderQty() != null ? offer.getMinOrderQty() : 1;

        // Validate offer configuration
        if (overridePrice == null || orderedQty < minOrderQty) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        // Only apply if override price is less than original price
        if (overridePrice.compareTo(unitPrice) >= 0) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        BigDecimal originalSubtotal = unitPrice.multiply(BigDecimal.valueOf(orderedQty));
        BigDecimal subtotal = overridePrice.multiply(BigDecimal.valueOf(orderedQty));
        BigDecimal discountAmount = originalSubtotal.subtract(subtotal);

        String description = String.format("Special price ₹%s (was ₹%s)", 
                overridePrice.stripTrailingZeros().toPlainString(),
                unitPrice.stripTrailingZeros().toPlainString());

        return OfferResult.builder()
                .billableQty(orderedQty)
                .deliveredQty(orderedQty)
                .subtotal(subtotal)
                .originalSubtotal(originalSubtotal)
                .discountAmount(discountAmount)
                .appliedOfferName(offer.getName())
                .appliedOfferId(offer.getId())
                .offerType(offer.getOfferType().name())
                .offerApplied(true)
                .offerDescription(description)
                .build();
    }

    @Override
    public boolean supports(Offer.OfferType offerType) {
        return Offer.OfferType.PRICE_OVERRIDE.equals(offerType);
    }

    @Override
    public Offer.OfferType getOfferType() {
        return Offer.OfferType.PRICE_OVERRIDE;
    }
}
