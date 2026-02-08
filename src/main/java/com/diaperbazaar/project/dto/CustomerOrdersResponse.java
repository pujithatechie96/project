package com.diaperbazaar.project.dto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerOrdersResponse {

    // Customer details
    private Long customerId;
    private String customerName;
    private String customerMobile;
    private Integer totalPoints;
    private BigDecimal redeemableAmount;
    private LocalDateTime customerCreatedAt;

    // Points summary
    private Integer totalPointsEarned;
    private Integer totalPointsRedeemed;

    // Order summary
    private Integer totalOrders;
    private BigDecimal totalOrderAmount;

    // All orders
    private List<CustomerOrderDTO> orders;

    // Points history
    private List<CustomerPointsLedgerDTO> pointsHistory;

    @Data
    public static class CustomerOrderDTO {
        private Long orderId;
        private BigDecimal totalAmount;
        private BigDecimal subtotal;
        private BigDecimal totalGst;
        private BigDecimal totalDiscount;
        private BigDecimal pointsDiscount;
        private Integer pointsUsed;
        private Integer pointsEarned;
        private String status;
        private String paymentMethod;
        private String paymentStatus;
        private LocalDateTime createdAt;
        private List<OrderItemDTO> items;
    }
}
