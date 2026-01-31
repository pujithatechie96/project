package com.diaperbazaar.project.strategy.impl;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;
import com.diaperbazaar.project.strategy.OfferStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BundlePriceStrategy - Implements Bundle/Combo pricing offers
 * Example: Buy 3 packs @ ₹999
 * 
 * Customer orders N items:
 * - Complete bundles = N / bundleQty
 * - Remaining items = N % bundleQty (charged at regular price)
 * - Total = (bundles * bundlePrice) + (remaining * unitPrice)
 */
@Component
public class BundlePriceStrategy implements OfferStrategy {

    @Override
    public OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice) {
        int bundleQty = offer.getBundleQty() != null ? offer.getBundleQty() : 1;
        BigDecimal bundlePrice = offer.getBundlePrice();
        int minOrderQty = offer.getMinOrderQty() != null ? offer.getMinOrderQty() : 1;

        // Validate offer configuration
        if (bundlePrice == null || orderedQty < minOrderQty || orderedQty < bundleQty) {
            return OfferResult.noOffer(orderedQty, unitPrice);
        }

        BigDecimal originalSubtotal = unitPrice.multiply(BigDecimal.valueOf(orderedQty));

        // Calculate complete bundles and remaining items
        int completeBundles = orderedQty / bundleQty;
        int remainingItems = orderedQty % bundleQty;

        // Calculate subtotal
        BigDecimal bundleTotal = bundlePrice.multiply(BigDecimal.valueOf(completeBundles));
        BigDecimal remainingTotal = unitPrice.multiply(BigDecimal.valueOf(remainingItems));
        BigDecimal subtotal = bundleTotal.add(remainingTotal);

        // Ensure we don't charge more than original price
        if (subtotal.compareTo(originalSubtotal) > 0) {
            subtotal = originalSubtotal;
        }

        BigDecimal discountAmount = originalSubtotal.subtract(subtotal);

        // Delivered = ordered (no free items in bundle pricing)
        int deliveredQty = orderedQty;
        int billableQty = orderedQty;

        String description = String.format("%d bundle(s) of %d @ ₹%s each", 
                completeBundles, bundleQty, bundlePrice.setScale(0, RoundingMode.HALF_UP));
        if (remainingItems > 0) {
            description += String.format(" + %d item(s) @ regular price", remainingItems);
        }

        return OfferResult.builder()
                .billableQty(billableQty)
                .deliveredQty(deliveredQty)
                .subtotal(subtotal)
                .originalSubtotal(originalSubtotal)
                .discountAmount(discountAmount)
                .appliedOfferName(offer.getName())
                .appliedOfferId(offer.getId())
                .offerType(offer.getOfferType().name())
                .offerApplied(completeBundles > 0)
                .offerDescription(description)
                .build();
    }

    @Override
    public boolean supports(Offer.OfferType offerType) {
        return Offer.OfferType.BUNDLE_PRICE.equals(offerType);
    }

    @Override
    public Offer.OfferType getOfferType() {
        return Offer.OfferType.BUNDLE_PRICE;
    }
}
