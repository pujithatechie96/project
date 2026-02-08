package com.diaperbazaar.project.dto;

import lombok.Data;

@Data
public class RedeemPointsRequest {
    private Long customerId;
    private Integer pointsToRedeem;
    private Long orderId;
}