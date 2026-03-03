package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Party Transaction Entity - Vyapar Style
 * Tracks money movements with parties (ledger)
 */
@Data
@Entity
@Table(name = "party_transactions")
public class PartyTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(nullable = false)
    private Long partyId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    // Payment details
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;
    
    private String paymentReference;
    
    // Balance after this transaction
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    // Reference to stock transaction (if applicable)
    private Long stockTransactionId;
    
    private String description;
    
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
        PURCHASE,      // We bought from them (Credit - we owe more)
        PAYMENT_OUT,   // We paid them (Debit - we owe less)
        SALE,          // They bought from us (Debit - they owe more)
        PAYMENT_IN,    // They paid us (Credit - they owe less)
        OPENING,
        RETURN// Opening balance
    }
    
    public enum PaymentMode {
        CASH,
        BANK,
        UPI,
        CHEQUE,
        OTHER
    }
}
