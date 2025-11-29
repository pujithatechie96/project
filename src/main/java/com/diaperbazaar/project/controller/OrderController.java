package com.diaperbazaar.project.controller;


import com.diaperbazaar.project.dto.CreateOrderRequest;
import com.diaperbazaar.project.dto.OrderDTO;
import com.diaperbazaar.project.entity.Order;
import com.diaperbazaar.project.entity.User;
import com.diaperbazaar.project.repository.UserRepository;
import com.diaperbazaar.project.service.OrderService;
import com.diaperbazaar.project.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        try {
            // Get user ID from authentication (adjust based on your auth implementation)
            String name = authentication.getName();
            Optional<User> user = userRepository.findByEmail(name);


            OrderDTO order = orderService.createOrder(user.get().getId(), request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getId());
            response.put("message", "Order placed successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        try {
            String name = authentication.getName();
            User user = userRepository.findByEmail(name).get();
            List<OrderDTO> orders = orderService.getUserOrders(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        try {
            String name = authentication.getName();
            User user = userRepository.findByEmail(name).get();
            OrderDTO order = orderService.getOrderById(orderId, user.getId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            Order.OrderStatus status = Order.OrderStatus.valueOf(request.get("status"));
            OrderDTO order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}