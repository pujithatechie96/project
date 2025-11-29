package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.CartItemRequest;
import com.diaperbazaar.project.entity.Cart;
import com.diaperbazaar.project.entity.User;
import com.diaperbazaar.project.repository.UserRepository;
import com.diaperbazaar.project.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('USER')")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartService.getOrCreateCart(user.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartService.addItemToCart(user.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Cart> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartService.updateCartItem(user.getId(), itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartService.removeItemFromCart(user.getId(), itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        cartService.clearCart(user.getId());
        return ResponseEntity.ok("Cart cleared successfully");
    }
}