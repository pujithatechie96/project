package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vendor_ledger")
public class VendorLedger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorTransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorReferenceType referenceType;
    
    private Long referenceId;
    private String referenceNumber;
    private String description;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;
    
    private String paymentReference;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    private Long createdBy;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
    
    public enum VendorTransactionType {
        OPENING_BALANCE,
        PURCHASE,
        PAYMENT,
        PURCHASE_RETURN,
        DISCOUNT_RECEIVED,
        ADJUSTMENT_CREDIT,
        ADJUSTMENT_DEBIT
    }
    
    public enum VendorReferenceType {
        PURCHASE_ORDER,
        PAYMENT,
        RETURN,
        ADJUSTMENT,
        MANUAL
    }
    
    public enum PaymentMode {
        CASH,
        BANK_TRANSFER,
        CHEQUE,
        UPI,
        CARD,
        OTHER
    }
}
