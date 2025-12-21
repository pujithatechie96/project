package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.CreateOrderRequest;
import com.diaperbazaar.project.dto.OrderDTO;
import com.diaperbazaar.project.dto.OrderItemDTO;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import com.diaperbazaar.project.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WalletService walletService;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // Calculate total amount
        BigDecimal subtotal = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply points discount if provided (1 point = ₹1)
        BigDecimal pointsDiscount = request.getPointsDiscount() != null ? request.getPointsDiscount() : BigDecimal.ZERO;
        BigDecimal amountAfterDiscount = subtotal.subtract(pointsDiscount);

        // Add tax (2%)
        BigDecimal tax = amountAfterDiscount.multiply(new BigDecimal("0.02"));
        BigDecimal finalAmount = amountAfterDiscount.add(tax);

        // Calculate points earned: 10% of subtotal (before tax and discount)
        // 1 point = ₹1, so 10% of subtotal = points earned
        int pointsEarned = finalAmount.multiply(new BigDecimal("0.10")).intValue();

        // Fetch address and convert to ShippingAddress JSON
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Verify address belongs to user
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }

        // Create ShippingAddress from Address for backward compatibility
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFullName(address.getFullName());
        shippingAddress.setPhone(address.getPhone());
        shippingAddress.setAddressLine1(address.getAddressLine1());
        shippingAddress.setAddressLine2(address.getAddressLine2());
        shippingAddress.setCity(address.getCity());
        shippingAddress.setState(address.getState());
        shippingAddress.setZipCode(address.getPincode());

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(finalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setAddressId(request.getAddressId());
        order.setShippingAddress(shippingAddress);
        order.setPointsUsed(request.getPointsUsed() != null ? request.getPointsUsed() : 0);
        order.setPointsDiscount(pointsDiscount);
        order.setPointsEarned(pointsEarned);

        // Create order items
        request.getItems().forEach(itemRequest -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setProductImage(itemRequest.getProductImage());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            item.setSize(itemRequest.getSize());
            ProductVariant variant = productVariantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            // 🔥 reduce stock
            variant.decreaseStock(itemRequest.getQuantity());

            item.setVariant(variant);
            order.addOrderItem(item);
        });

        // Save order
        Order savedOrder = orderRepository.save(order);

        // 🔥 After saving order, credit points to wallet
        // Points earned = 10% of subtotal
        walletService.creditPoints(
                userId,
                pointsEarned,
                "Points earned from Order #" + savedOrder.getId(),
                savedOrder.getId()
        );

        // 🔥 If points were used, debit them
        if (request.getPointsUsed() != null && request.getPointsUsed() > 0) {
            walletService.debitPoints(
                    userId,
                    request.getPointsUsed(),
                    "Points redeemed for Order #" + savedOrder.getId(),
                    savedOrder.getId()
            );
        }

        return convertToDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("Order not found");
        }
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setAddressId(order.getAddressId());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPointsUsed(order.getPointsUsed());
        dto.setPointsDiscount(order.getPointsDiscount());
        dto.setPointsEarned(order.getPointsEarned());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProductId());
                    itemDTO.setProductName(item.getProductName());
                    itemDTO.setProductImage(item.getProductImage());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setSize(item.getSize());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setOrderItems(items);
        return dto;
    }
}
