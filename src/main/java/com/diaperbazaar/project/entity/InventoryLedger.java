package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inventory_ledger")
public class InventoryLedger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;
    
    private Long referenceId;
    private String referenceNumber;
    
    @Column(nullable = false)
    private Long productId;
    
    private Long variantId;
    private Long vendorId;
    
    private Integer quantityIn = 0;
    private Integer quantityOut = 0;
    
    @Column(nullable = false)
    private Integer balanceQuantity;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal totalValue;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    private Long createdBy;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Transient
    private String productName;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
    
    public enum TransactionType {
        PURCHASE,
        PURCHASE_RETURN,
        SALE,
        SALE_RETURN,
        ADJUSTMENT_IN,
        ADJUSTMENT_OUT,
        TRANSFER_IN,
        TRANSFER_OUT,
        DAMAGE,
        EXPIRED,
        OPENING_STOCK
    }
    
    public enum ReferenceType {
        PURCHASE_ORDER,
        SALE_ORDER,
        ADJUSTMENT,
        TRANSFER,
        MANUAL
    }
}
