package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.UpdateOrderRequest;
import com.diaperbazaar.project.entity.User;
import com.diaperbazaar.project.repository.UserRepository;
import com.diaperbazaar.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminOrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllOrders(Authentication authentication) {
        String name = authentication.getName();
        Optional<User> user = userRepository.findByEmail(name);
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId,Authentication authentication) {
        String name = authentication.getName();
        Optional<User> user = userRepository.findByEmail(name);
        return ResponseEntity.ok(orderService.getOrderByIdAdmin(orderId));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderRequest request,Authentication authentication) {
        String name = authentication.getName();
        Optional<User> user = userRepository.findByEmail(name);

        return ResponseEntity.ok(orderService.updateOrderAdmin(orderId, request));
    }
}
