package com.diaperbazaar.project.strategy.impl;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.strategy.OfferStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * BuyXGetYStrategy - Implements Buy X Get Y Free offers
 * Example: Buy 2 Get 1 Free
 * 
 * Customer orders N items:
 * - Groups = N / (buyQty + getQty)
 * - Billable = Groups * buyQty + Remaining (if remaining >= buyQty, partial logic applies)
 * - Delivered = N (customer gets what they ordered)
 */
@Component
public class BuyXGetYStrategy implements OfferStrategy {

    @Override
    public OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice) {
        int buyQty = offer.getBuyQty() != null ? offer.getBuyQty() : 1;
        int getQty = offer.getGetQty() != null ? offer.getGetQty() : 0;
        int minOrderQty = offer.getMinOrderQty() != null ? offer.getMinOrderQty() : 1;

        // Check minimum order quantity
        if (orderedQty < minOrderQty || orderedQty < buyQty) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        BigDecimal originalSubtotal = unitPrice.multiply(BigDecimal.valueOf(orderedQty));

        // Calculate how many complete cycles of buy+get
        int cycleSize = buyQty + getQty;
        int completeCycles = orderedQty / cycleSize;
        int remaining = orderedQty % cycleSize;

        // Billable quantity: customer pays for buyQty per cycle + remaining items
        int billableQty = completeCycles * buyQty;
        
        // Handle remaining items
        if (remaining > 0) {
            // If remaining is more than buyQty, they get some free
            if (remaining > buyQty) {
                billableQty += buyQty;
            } else {
                // Not enough for free items, pay for all remaining
                billableQty += remaining;
            }
        }

        // Delivered quantity is always what they ordered
        int deliveredQty = orderedQty;

        // Calculate subtotal (pay only for billable quantity)
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(billableQty));
        BigDecimal discountAmount = originalSubtotal.subtract(subtotal);

        // Ensure subtotal is not negative
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
            discountAmount = originalSubtotal;
        }

        int freeQty = deliveredQty - billableQty;
        String description = String.format("Buy %d Get %d Free: %d items free", 
                buyQty, getQty, freeQty);

        return OfferResult.builder()
                .billableQty(billableQty)
                .deliveredQty(deliveredQty)
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
        return Offer.OfferType.BUY_X_GET_Y.equals(offerType);
    }

    @Override
    public Offer.OfferType getOfferType() {
        return Offer.OfferType.BUY_X_GET_Y;
    }
}
