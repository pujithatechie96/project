package com.diaperbazaar.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_sizes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "size"})
        }
)
@Getter
@Setter
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= RELATION ================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    /* ================= SIZE INFO ================= */

    @Column(nullable = false)
    private String size; // Newborn, S, M, L, XL, XXL

    @Column(nullable = false)
    private Integer stock = 0;

    /* ================= PRICING ================= */

    @Column(name = "original_price", nullable = false)
    private Double originalPrice;

    @Column(name = "buy_price", nullable = false)
    private Double buyPrice;

    @Column(name = "sell_price", nullable = false)
    private Double sellPrice;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    /* ================= EXTRA ================= */

    @Enumerated(EnumType.STRING)
    @Column(name = "wear_type")
    private WearType wearType; // PANT / TAPE / null

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(unique = true)
    private String sku;

    /* ================= AUDIT ================= */

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        calculateDiscount();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        calculateDiscount();
    }

    private void calculateDiscount() {
        if (originalPrice != null && originalPrice > 0 && sellPrice != null) {
            this.discountPercentage =
                    Math.round(((originalPrice - sellPrice) / originalPrice) * 10000.0) / 100.0;
        }
    }

    public boolean isDefault() {
        return isDefault;
    }

    /* ================= ENUM ================= */

    public enum WearType {
        PANT,
        TAPE
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public WearType getWearType() {
        return wearType;
    }

    public void setWearType(WearType wearType) {
        this.wearType = wearType;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
