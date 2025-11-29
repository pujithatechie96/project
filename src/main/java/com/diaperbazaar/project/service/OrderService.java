package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.CreateOrderRequest;
import com.diaperbazaar.project.dto.OrderDTO;
import com.diaperbazaar.project.dto.OrderItemDTO;
import com.diaperbazaar.project.entity.Order;
import com.diaperbazaar.project.entity.OrderItem;
import com.diaperbazaar.project.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // Calculate total amount
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());

        // Create order items
        request.getItems().forEach(itemRequest -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setProductImage(itemRequest.getProductImage());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            item.setSize(itemRequest.getSize());
            order.addOrderItem(item);
        });

        // Save order
        Order savedOrder = orderRepository.save(order);

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
        dto.setShippingAddress(order.getShippingAddress());
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