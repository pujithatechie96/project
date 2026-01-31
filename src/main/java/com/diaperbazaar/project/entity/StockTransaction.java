package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stock Transaction Entity - Vyapar Style
 * Tracks all stock movements (purchases, sales, adjustments)
 */
@Data
@Entity
@Table(name = "stock_transactions")
public class StockTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    
    // Product Reference
    @Column(nullable = false)
    private Long productId;
    
    private Long variantId;
    
    private String productName; // Denormalized for faster display
    
    // Party Reference (for purchases/sales)
    private Long partyId;
    private String partyName; // Denormalized for faster display
    
    // Quantity Movement
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // Running Balance (stock after this transaction)
    @Column(nullable = false)
    private Integer balanceAfter = 0;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
    
    public enum TransactionType {
        PURCHASE,      // Bought from supplier (Stock IN)
        SALE,          // Sold to customer (Stock OUT)
        STOCK_IN,      // Manual addition (Stock IN)
        STOCK_OUT,     // Manual removal (Stock OUT)
        DAMAGE,        // Damaged goods (Stock OUT)
        OPENING        // Opening stock (Stock IN)
    }
    
    // Helper method to check if this is a stock-in transaction
    public boolean isStockIn() {
        return transactionType == TransactionType.PURCHASE 
            || transactionType == TransactionType.STOCK_IN 
            || transactionType == TransactionType.OPENING;
    }
}
