package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.Order;
import com.diaperbazaar.project.entity.OrderItem;
import com.diaperbazaar.project.entity.ShippingAddress;
import com.diaperbazaar.project.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private Long userId;
    private BigDecimal totalAmount;

    private Order.OrderStatus status;
    private String paymentMethod;
    private Order.PaymentStatus paymentStatus;

    private Long addressId;
    private ShippingAddress shippingAddress;

    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private Integer pointsEarned;

    private List<OrderItemDTO> orderItems;

    private BigDecimal totalDiscount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ ENTITY → DTO MAPPER
    public static OrderDTO fromEntity(Order order) {

        OrderDTO dto = new OrderDTO();

        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());

        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());

        dto.setPointsUsed(order.getPointsUsed());
        dto.setPointsDiscount(order.getPointsDiscount());
        dto.setPointsEarned(order.getPointsEarned());

        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(order.getShippingAddress());
            dto.setAddressId(order.getAddressId());
        }

        if (order.getOrderItems() != null) {
            dto.setOrderItems(
                    order.getOrderItems()
                            .stream()
                            .map(OrderItemDTO::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        dto.setTotalDiscount(order.getTotalDiscount());

        return dto;
    }
}
