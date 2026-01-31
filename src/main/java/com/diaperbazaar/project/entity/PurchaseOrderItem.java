package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;
    
    @Column(nullable = false)
    private Long productId;
    
    private Long variantId;
    
    @Column(nullable = false)
    private Integer quantityOrdered;
    
    private Integer quantityReceived = 0;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    private String notes;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Transient
    private String productName;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }
    
    public void calculateTotals() {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantityOrdered));
        this.discountAmount = subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal taxable = subtotal.subtract(discountAmount);
        this.taxAmount = taxable.multiply(taxPercentage).divide(BigDecimal.valueOf(100));
        this.totalAmount = taxable.add(taxAmount);
    }
}
