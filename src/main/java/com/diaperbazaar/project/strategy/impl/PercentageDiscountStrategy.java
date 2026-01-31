package com.diaperbazaar.project.strategy.impl;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.strategy.OfferStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PercentageDiscountStrategy - Implements percentage-based discount offers
 * Example: 15% off (max ₹200 discount)
 * 
 * Discount = (subtotal * percentage / 100), capped at maxDiscount if specified
 */
@Component
public class PercentageDiscountStrategy implements OfferStrategy {

    @Override
    public OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice) {
        BigDecimal discountPercentage = offer.getDiscountValue();
        BigDecimal maxDiscount = offer.getMaxDiscount();
        int minOrderQty = offer.getMinOrderQty() != null ? offer.getMinOrderQty() : 1;

        // Validate offer configuration
        if (discountPercentage == null || orderedQty < minOrderQty) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        BigDecimal originalSubtotal = unitPrice.multiply(BigDecimal.valueOf(orderedQty));

        // Calculate percentage discount
        BigDecimal discountAmount = originalSubtotal
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Apply max discount cap if specified
        if (maxDiscount != null && discountAmount.compareTo(maxDiscount) > 0) {
            discountAmount = maxDiscount;
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

        String description = String.format("%s%% off applied (₹%s saved)", 
                discountPercentage.stripTrailingZeros().toPlainString(),
                discountAmount.setScale(2, RoundingMode.HALF_UP));
        
        if (maxDiscount != null && discountAmount.compareTo(maxDiscount) >= 0) {
            description += " (max discount reached)";
        }

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
        return Offer.OfferType.PERCENT_DISCOUNT.equals(offerType);
    }

    @Override
    public Offer.OfferType getOfferType() {
        return Offer.OfferType.PERCENT_DISCOUNT;
    }
}
