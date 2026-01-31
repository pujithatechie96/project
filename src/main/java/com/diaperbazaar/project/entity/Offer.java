package com.diaperbazaar.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Offer Entity - Represents all types of offers in the system
 * Supports: BUY_X_GET_Y, BUNDLE_PRICE, FLAT_DISCOUNT, PERCENT_DISCOUNT, PRICE_OVERRIDE, FREE_ITEM
 */
@Entity
@Table(name = "offers", indexes = {
    @Index(name = "idx_offer_type", columnList = "offer_type"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_date_range", columnList = "start_date, end_date"),
    @Index(name = "idx_priority", columnList = "priority")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false)
    private OfferType offerType;

    // BUY_X_GET_Y specific fields
    @Column(name = "buy_qty")
    @Builder.Default
    private Integer buyQty = 1;

    @Column(name = "get_qty")
    @Builder.Default
    private Integer getQty = 0;

    // BUNDLE_PRICE specific fields
    @Column(name = "bundle_qty")
    @Builder.Default
    private Integer bundleQty = 1;

    @Column(name = "bundle_price", precision = 10, scale = 2)
    private BigDecimal bundlePrice;

    // FLAT_DISCOUNT & PERCENT_DISCOUNT fields
    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    // PRICE_OVERRIDE specific fields
    @Column(name = "override_price", precision = 10, scale = 2)
    private BigDecimal overridePrice;

    // FREE_ITEM specific fields
    @Column(name = "free_variant_id")
    private Long freeVariantId;

    @Column(name = "free_item_qty")
    @Builder.Default
    private Integer freeItemQty = 1;

    // Validity and status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    // Constraints
    @Column(name = "min_order_qty")
    @Builder.Default
    private Integer minOrderQty = 1;

    @Column(name = "max_usage_per_order")
    private Integer maxUsagePerOrder;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    // Mapped product variants
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<OfferProductVariant> productVariants = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if offer is currently valid (active and within date range)
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive != null && isActive 
            && startDate != null && endDate != null
            && !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /**
     * Offer Type Enum
     */
    public enum OfferType {
        BUY_X_GET_Y,      // Buy X items, Get Y free
        BUNDLE_PRICE,     // Fixed price for bundle quantity
        FLAT_DISCOUNT,    // Fixed amount discount
        PERCENT_DISCOUNT, // Percentage discount with optional cap
        PRICE_OVERRIDE,   // Replace price with fixed offer price
        FREE_ITEM         // Add free product variant to order
    }
}
