package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for sale entry (Vyapar style - stock out to customer)
 */
@Data
public class SaleEntryDTO {
    private Long productId;
    private Long variantId;
    private Long partyId; // Optional - customer
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime transactionDate;
    private String notes;
}
