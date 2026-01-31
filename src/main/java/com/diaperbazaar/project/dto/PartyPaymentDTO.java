package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for party payment (payment in/out)
 */
@Data
public class PartyPaymentDTO {
    private Long partyId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentMode; // CASH, BANK, UPI, CHEQUE, OTHER
    private String paymentReference;
    private String notes;
}
