package com.diaperbazaar.project.strategy;

import com.diaperbazaar.project.dto.OfferResult;
import com.diaperbazaar.project.entity.Offer;

import java.math.BigDecimal;

/**
 * OfferStrategy Interface - Strategy Pattern for offer calculations
 * Each offer type implements this interface with its specific calculation logic
 */
public interface OfferStrategy {

    /**
     * Apply the offer and calculate the result
     *
     * @param offer      The offer configuration
     * @param orderedQty Quantity ordered by customer
     * @param unitPrice  Unit price of the product variant
     * @return OfferResult with calculated billable qty, delivered qty, and subtotal
     */
    OfferResult apply(Offer offer, int orderedQty, BigDecimal unitPrice);

    /**
     * Check if this strategy can handle the given offer type
     *
     * @param offerType The offer type to check
     * @return true if this strategy can handle the offer type
     */
    boolean supports(Offer.OfferType offerType);

    /**
     * Get the offer type this strategy handles
     *
     * @return The OfferType this strategy is designed for
     */
    Offer.OfferType getOfferType();
}
