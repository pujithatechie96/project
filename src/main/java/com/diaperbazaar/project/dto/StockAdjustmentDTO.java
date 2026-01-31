package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for stock adjustment (Stock In / Stock Out / Damage / Opening)
 */
@Data
public class StockAdjustmentDTO {
    private Long productId;
    private Long variantId;
    private String adjustmentType; // STOCK_IN, STOCK_OUT, DAMAGE, OPENING
    private Integer quantity;
    private BigDecimal unitPrice; // Optional, for valuation
    private LocalDateTime transactionDate;
    private String notes;
    private Long partyId;
    private String partyName;
}
