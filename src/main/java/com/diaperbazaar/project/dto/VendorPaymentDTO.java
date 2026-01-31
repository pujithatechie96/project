package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class VendorPaymentDTO {
    private Long vendorId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMode;
    private String paymentReference;
    private String bankName;
    private String chequeNumber;
    private LocalDate chequeDate;
    private String notes;
    private List<AllocationDTO> allocations;
    
    @Data
    public static class AllocationDTO {
        private Long purchaseOrderId;
        private BigDecimal allocatedAmount;
    }
}
