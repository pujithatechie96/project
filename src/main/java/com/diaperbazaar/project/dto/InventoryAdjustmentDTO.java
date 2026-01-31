package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryAdjustmentDTO {
    private Long productId;
    private Long variantId;
    private String transactionType; // ADJUSTMENT_IN, ADJUSTMENT_OUT, DAMAGE, EXPIRED
    private Integer quantity;
    private BigDecimal unitCost;
    private String notes;
}
