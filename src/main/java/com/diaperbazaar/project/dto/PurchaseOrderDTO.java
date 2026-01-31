package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderDTO {
    private Long vendorId;
    private String orderDate;
    private String expectedDeliveryDate;
    private String notes;
    private List<ItemDTO> items;
    
    @Data
    public static class ItemDTO {
        private Long productId;
        private Long variantId;
        private Integer quantityOrdered;
        private BigDecimal unitPrice;
        private BigDecimal taxPercentage;
        private BigDecimal discountPercentage;
    }
}
