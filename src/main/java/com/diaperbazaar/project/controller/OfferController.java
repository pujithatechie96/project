package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Offer Controller - REST API for offer management
 */
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OfferController {

    private final OfferService offerService;

    // =====================================================
    // CRUD ENDPOINTS
    // =====================================================

    /**
     * GET /api/offers
     * Get all offers with pagination
     */
    @GetMapping
    public ResponseEntity<Page<Offer>> getAllOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(offerService.searchOffers(search, pageable));
        }
        return ResponseEntity.ok(offerService.getAllOffers(pageable));
    }

    /**
     * GET /api/offers/{id}
     * Get offer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return offerService.getOfferById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/offers
     * Create a new offer
     */
    @PostMapping
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer) {
        try {
            Offer created = offerService.createOffer(offer);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/offers/{id}
     * Update an existing offer
     */
    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer offer) {
        try {
            Offer updated = offerService.updateOffer(id, offer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/offers/{id}
     * Delete an offer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        try {
            offerService.deleteOffer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/offers/{id}/toggle
     * Toggle offer active status
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Offer> toggleOfferStatus(@PathVariable Long id) {
        try {
            Offer updated = offerService.toggleOfferStatus(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =====================================================
    // PRODUCT VARIANT MAPPING ENDPOINTS
    // =====================================================

    /**
     * GET /api/offers/{id}/variants
     * Get all variant IDs mapped to an offer
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<Long>> getOfferVariants(@PathVariable Long id) {
        List<Long> variantIds = offerService.getVariantIdsForOffer(id);
        return ResponseEntity.ok(variantIds);
    }

    /**
     * GET /api/offers/{id}/mappings
     * Get all product-variant mappings for an offer
     */
    @GetMapping("/{id}/mappings")
    public ResponseEntity<List<Map<String, Long>>> getOfferMappings(@PathVariable Long id) {
        List<OfferProductVariant> mappings = offerService.getMappingsForOffer(id);
        List<Map<String, Long>> result = mappings.stream()
                .map(m -> Map.of(
                        "productId", m.getProductId(),
                        "variantId", m.getProductVariantId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/offers/{id}/variants
     * Add variants to an offer with product IDs
     */
    @PostMapping("/{id}/variants")
    public ResponseEntity<Void> addVariantsToOffer(
            @PathVariable Long id,
            @RequestBody List<OfferService.ProductVariantMappingInput> mappings) {
        offerService.addVariantsToOffer(id, mappings);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/offers/{id}/variants
     * Replace all variants for an offer
     */
    @PutMapping("/{id}/variants")
    public ResponseEntity<Void> replaceOfferVariants(
            @PathVariable Long id,
            @RequestBody List<OfferService.ProductVariantMappingInput> mappings) {
        offerService.replaceVariantsForOffer(id, mappings);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/offers/{id}/products/{productId}/variants/{variantId}
     * Remove a variant from an offer
     */
    @DeleteMapping("/{id}/products/{productId}/variants/{variantId}")
    public ResponseEntity<Void> removeVariantFromOffer(
            @PathVariable Long id,
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        offerService.removeVariantFromOffer(id, productId, variantId);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // OFFER APPLICATION ENDPOINTS
    // =====================================================

    /**
     * GET /api/offers/variant/{variantId}
     * Get all active offers for a product variant
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<Offer>> getOffersForVariant(@PathVariable Long variantId) {
        List<Offer> offers = offerService.getActiveOffersForVariant(variantId);
        return ResponseEntity.ok(offers);
    }

    /**
     * GET /api/offers/variant/{variantId}/best
     * Get the best (highest priority) offer for a variant
     */
    @GetMapping("/variant/{variantId}/best")
    public ResponseEntity<Offer> getBestOfferForVariant(@PathVariable Long variantId) {
        return offerService.getBestOfferForVariant(variantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/preview")
    public ResponseEntity<List<OfferService.CartItemOfferPreview>> previewCartOffers(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        List<OfferService.CartItemOfferPreview> previews = items.stream()
                .map(item -> {
                    Long variantId = ((Number) item.get("variantId")).longValue();
                    int quantity = ((Number) item.get("quantity")).intValue();
                    BigDecimal unitPrice = new BigDecimal(item.get("unitPrice").toString());

                    return offerService.previewCartItemOffer(variantId, quantity, unitPrice);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(previews);
    }

    /**
     * POST /api/offers/{id}/preview
     * Preview offer calculation without applying
     */
    @PostMapping("/{id}/preview")
    public ResponseEntity<OfferResult> previewOffer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        int quantity = ((Number) request.get("quantity")).intValue();
        BigDecimal unitPrice = new BigDecimal(request.get("unitPrice").toString());
        
        OfferResult result = offerService.previewOffer(id, quantity, unitPrice);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/offers/calculate
     * Calculate best offer for a variant with given quantity
     */
    @PostMapping("/calculate")
    public ResponseEntity<OfferResult> calculateOffer(@RequestBody Map<String, Object> request) {
        Long variantId = ((Number) request.get("variantId")).longValue();
        int quantity = ((Number) request.get("quantity")).intValue();
        BigDecimal unitPrice = new BigDecimal(request.get("unitPrice").toString());
        
        OfferResult result = offerService.applyBestOffer(variantId, quantity, unitPrice);
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // UTILITY ENDPOINTS
    // =====================================================

    /**
     * GET /api/offers/stats
     * Get offer statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<OfferService.OfferStats> getOfferStats() {
        return ResponseEntity.ok(offerService.getOfferStats());
    }

    /**
     * GET /api/offers/types
     * Get all offer types
     */
    @GetMapping("/types")
    public ResponseEntity<Offer.OfferType[]> getOfferTypes() {
        return ResponseEntity.ok(Offer.OfferType.values());
    }

    /**
     * POST /api/offers/deactivate-expired
     * Deactivate all expired offers
     */
    @PostMapping("/deactivate-expired")
    public ResponseEntity<Map<String, Integer>> deactivateExpiredOffers() {
        int count = offerService.deactivateExpiredOffers();
        return ResponseEntity.ok(Map.of("deactivatedCount", count));
    }
}
