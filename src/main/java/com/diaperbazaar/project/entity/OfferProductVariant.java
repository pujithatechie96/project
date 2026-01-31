package com.diaperbazaar.project.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * OfferProductVariant Entity - Maps offers to products and variants (many-to-many)
 */
@Entity
@Table(name = "offer_product_variants", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"offer_id", "product_id", "product_variant_id"}),
    indexes = {
        @Index(name = "idx_opv_offer", columnList = "offer_id"),
        @Index(name = "idx_opv_product", columnList = "product_id"),
        @Index(name = "idx_opv_variant", columnList = "product_variant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
