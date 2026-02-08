package com.diaperbazaar.project.dto;

import lombok.Data;
import java.math.BigDecimal;
@Data
public class RedeemPointsResponse {
    private Integer pointsRedeemed;
    private BigDecimal discountAmount;
    private Integer remainingPoints;
    private CustomerDTO customer;
}