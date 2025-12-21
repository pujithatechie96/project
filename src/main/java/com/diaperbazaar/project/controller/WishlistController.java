package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.WishlistItemDTO;
import com.diaperbazaar.project.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> getWishlist(@RequestParam Long userId) {
        List<WishlistItemDTO> wishlist = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addToWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Long variantId) {
        wishlistService.addToWishlist(userId,productId,variantId);
        return ResponseEntity.ok(Map.of("message", "Product added to wishlist"));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeFromWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("message", "Product removed from wishlist"));
    }
}
