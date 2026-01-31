package com.diaperbazaar.project.strategy;

import com.diaperbazaar.project.entity.Offer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * OfferStrategyFactory - Factory to get the appropriate strategy for an offer type
 * Uses Spring's dependency injection to automatically register all strategies
 */
@Component
public class OfferStrategyFactory {

    private final List<OfferStrategy> strategies;
    private final Map<Offer.OfferType, OfferStrategy> strategyMap;

    public OfferStrategyFactory(List<OfferStrategy> strategies) {
        this.strategies = strategies;
        this.strategyMap = new EnumMap<>(Offer.OfferType.class);
    }

    /**
     * Initialize the strategy map after construction
     * Maps each OfferType to its corresponding strategy implementation
     */
    @PostConstruct
    public void init() {
        for (OfferStrategy strategy : strategies) {
            strategyMap.put(strategy.getOfferType(), strategy);
        }
    }

    /**
     * Get the strategy for a specific offer type
     *
     * @param offerType The type of offer
     * @return The appropriate OfferStrategy implementation
     * @throws IllegalArgumentException if no strategy found for the offer type
     */
    public OfferStrategy getStrategy(Offer.OfferType offerType) {
        OfferStrategy strategy = strategyMap.get(offerType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for offer type: " + offerType);
        }
        return strategy;
    }

    /**
     * Get the strategy for a specific offer
     *
     * @param offer The offer entity
     * @return The appropriate OfferStrategy implementation
     */
    public OfferStrategy getStrategy(Offer offer) {
        return getStrategy(offer.getOfferType());
    }

    /**
     * Check if a strategy exists for the given offer type
     *
     * @param offerType The offer type to check
     * @return true if a strategy exists
     */
    public boolean hasStrategy(Offer.OfferType offerType) {
        return strategyMap.containsKey(offerType);
    }

    /**
     * Get all registered strategies
     *
     * @return Map of offer types to their strategies
     */
    public Map<Offer.OfferType, OfferStrategy> getAllStrategies() {
        return new EnumMap<>(strategyMap);
    }
}
