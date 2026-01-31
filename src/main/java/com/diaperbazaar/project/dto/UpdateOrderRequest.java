package com.diaperbazaar.project.dto;


import com.diaperbazaar.project.entity.Order;
import lombok.Data;

@Data
public class UpdateOrderRequest {

    private Order.OrderStatus status;

    private Long addressId;

    private String paymentMethod;
}

