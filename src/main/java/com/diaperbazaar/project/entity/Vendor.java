package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vendors")
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String companyName;
    private String email;
    private String phone;
    private String alternatePhone;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    private String city;
    private String state;
    private String pincode;
    private String gstNumber;
    private String panNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfscCode;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;
    
    private Integer creditPeriodDays = 30;
    private Boolean isActive = true;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(updatable = false)
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
}
