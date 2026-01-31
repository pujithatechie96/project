package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Party Entity - Vyapar Style
 * Represents vendors/suppliers/customers
 */
@Data
@Entity
@Table(name = "parties")
public class Party {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String phone;
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    private PartyType partyType = PartyType.SUPPLIER;
    
    private String gstNumber;
    
    // Balance: Positive = We owe them, Negative = They owe us
    @Column(precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    private Boolean isActive = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PartyType {
        SUPPLIER,
        CUSTOMER,
        BOTH
    }
}
