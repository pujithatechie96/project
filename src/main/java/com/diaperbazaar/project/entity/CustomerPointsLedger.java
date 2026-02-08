package com.diaperbazaar.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "customer_points_ledger")
public class CustomerPointsLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "order_id")
    private Long orderId;
    @Column(nullable = false)
    private Integer points;
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    @Column(length = 500)
    private String description;
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    public enum TransactionType {
        CREDIT, DEBIT
    }
}