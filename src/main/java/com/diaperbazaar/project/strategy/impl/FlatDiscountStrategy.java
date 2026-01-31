package com.diaperbazaar.project.strategy.impl;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.strategy.OfferStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * FlatDiscountStrategy - Implements flat amount discount offers
 * Example: ₹50 off on the order
 * 
 * Discount is applied once per order item (or per quantity based on config)
 */
@Component
public class FlatDiscountStrategy implements OfferStrategy {

    @Override
    public OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice) {
        BigDecimal discountValue = offer.getDiscountValue();
        int minOrderQty = offer.getMinOrderQty() != null ? offer.getMinOrderQty() : 1;

        // Validate offer configuration
        if (discountValue == null || orderedQty < minOrderQty) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        BigDecimal originalSubtotal = unitPrice.multiply(BigDecimal.valueOf(orderedQty));

        // Apply flat discount (once per line item, not per unit)
        BigDecimal discountAmount = discountValue;

        // Handle max usage per order
        if (offer.getMaxUsagePerOrder() != null && offer.getMaxUsagePerOrder() > 1) {
            int applicableTimes = Math.min(orderedQty, offer.getMaxUsagePerOrder());
            discountAmount = discountValue.multiply(BigDecimal.valueOf(applicableTimes));
        }

        // Ensure discount doesn't exceed subtotal
        if (discountAmount.compareTo(originalSubtotal) > 0) {
            discountAmount = originalSubtotal;
        }

        BigDecimal subtotal = originalSubtotal.subtract(discountAmount);

        // Ensure subtotal is not negative
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
            discountAmount = originalSubtotal;
        }

        String description = String.format("Flat ₹%s off applied", 
                discountValue.stripTrailingZeros().toPlainString());

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
        return Offer.OfferType.FLAT_DISCOUNT.equals(offerType);
    }

    @Override
    public Offer.OfferType getOfferType() {
        return Offer.OfferType.FLAT_DISCOUNT;
    }
}
