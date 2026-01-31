//package com.diaperbazaar.project.entity;
//
//import com.diaperbazaar.project.dto.VendorPaymentDTO;
//import jakarta.persistence.*;
//import lombok.Data;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//@Entity
//@Table(name = "vendor_payments")
//public class VendorPayment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String paymentNumber;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "vendor_id", nullable = false)
//    private Vendor vendor;
//
//    @Column(nullable = false)
//    private LocalDate paymentDate;
//
//    @Column(nullable = false, precision = 15, scale = 2)
//    private BigDecimal amount;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private PaymentMode paymentMode;
//
//    private String paymentReference;
//    private String bankName;
//    private String chequeNumber;
//    private LocalDate chequeDate;
//
//    @Enumerated(EnumType.STRING)
//    private PaymentStatus status = PaymentStatus.COMPLETED;
//
//    @Column(columnDefinition = "TEXT")
//    private String notes;
//
//    private Long createdBy;
//
//    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<VendorPayment> allocations = new ArrayList<>();
//
//    @Column(updatable = false)
//    private LocalDateTime createdAt;
//
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        updatedAt = LocalDateTime.now();
//    }
//
//    public enum PaymentMode {
//        CASH,
//        BANK_TRANSFER,
//        CHEQUE,
//        UPI,
//        CARD,
//        OTHER
//    }
//
//    public enum PaymentStatus {
//        PENDING,
//        COMPLETED,
//        BOUNCED,
//        CANCELLED
//    }
//}
