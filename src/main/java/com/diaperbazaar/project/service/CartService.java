package com.diaperbazaar.project.service;


import com.diaperbazaar.project.dto.CartItemRequest;
import com.diaperbazaar.project.entity.Cart;
import com.diaperbazaar.project.entity.CartItem;
import com.diaperbazaar.project.entity.Product;
import com.diaperbazaar.project.entity.User;
import com.diaperbazaar.project.repository.CartRepository;
import com.diaperbazaar.project.repository.ProductRepository;
import com.diaperbazaar.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Cart getOrCreateCart(Long userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (cartOptional.isPresent()) {
            return cartOptional.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    public Cart addItemToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()) &&
                        (request.getSize() == null || request.getSize().equals(item.getSize())))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setSize(request.getSize());
            cart.getItems().add(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public Cart updateCartItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
