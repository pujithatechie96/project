package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for purchase entry (Vyapar style - stock in from party)
 */
@Data
public class PurchaseEntryDTO {
    private Long productId;
    private Long variantId;
    private Long partyId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime transactionDate;
    private String notes;
}
