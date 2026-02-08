package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.CustomerPointsLedger;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CustomerPointsLedgerDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long orderId;
    private Integer points;
    private String transactionType;
    private String description;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
    public static CustomerPointsLedgerDTO fromEntity(CustomerPointsLedger ledger) {
        CustomerPointsLedgerDTO dto = new CustomerPointsLedgerDTO();
        dto.setId(ledger.getId());
        dto.setCustomerId(ledger.getCustomer().getId());
        dto.setCustomerName(ledger.getCustomer().getName());
        dto.setOrderId(ledger.getOrderId());
        dto.setPoints(ledger.getPoints());
        dto.setTransactionType(ledger.getTransactionType().name());
        dto.setDescription(ledger.getDescription());
        dto.setBalanceAfter(ledger.getBalanceAfter());
        dto.setCreatedAt(ledger.getCreatedAt());
        return dto;
    }
}