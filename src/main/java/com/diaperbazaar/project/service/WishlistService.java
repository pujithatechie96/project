package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.WishlistItemDTO;
import com.diaperbazaar.project.entity.Product;
import com.diaperbazaar.project.entity.ProductVariant;
import com.diaperbazaar.project.entity.Wishlist;
import com.diaperbazaar.project.repository.ProductRepository;
import com.diaperbazaar.project.repository.ProductVariantRepository;
import com.diaperbazaar.project.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    /* ================= GET WISHLIST ================= */

    public List<WishlistItemDTO> getWishlist(Long userId) {
        List<Wishlist> wishlistItems =
                wishlistRepository.findByUserIdWithProductAndVariant(userId);

        return wishlistItems.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /* ================= ADD TO WISHLIST ================= */

    @Transactional
    public void addToWishlist(Long userId, Long productId, Long variantId) {

        if (wishlistRepository.existsByUserIdAndVariantId(userId, variantId)) {
            throw new RuntimeException("Variant already in wishlist");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Variant does not belong to product");
        }

        Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .product(product)
                .variant(variant)
                .build();

        wishlistRepository.save(wishlist);
    }

    /* ================= REMOVE FROM WISHLIST ================= */

    @Transactional
    public void removeFromWishlist(Long userId, Long variantId) {
        wishlistRepository.deleteByUserIdAndVariantId(userId, variantId);
    }

    /* ================= CHECK ================= */

    public boolean isInWishlist(Long userId, Long variantId) {
        return wishlistRepository.existsByUserIdAndVariantId(userId, variantId);
    }

    /* ================= MAPPER ================= */

    private WishlistItemDTO mapToDTO(Wishlist wishlist) {

        Product product = wishlist.getProduct();
        ProductVariant variant = wishlist.getVariant();

        List<String> availableSizes = product.getVariants().stream()
                .filter(v -> v.getStock() > 0)
                .map(ProductVariant::getSize)
                .distinct()
                .toList();

        String image = null;
        if (variant.getImages() != null && !variant.getImages().isBlank()) {
            image = variant.getImages().split(",")[0].trim();
        }

        return WishlistItemDTO.builder()
                .id(wishlist.getId())

                /* ---------- PRODUCT ---------- */
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .category(product.getCategory() != null
                        ? product.getCategory().getName()
                        : null)

                /* ---------- VARIANT ---------- */
                .variantId(variant.getId())
                .variantTitle(variant.getTitle())
                .size(variant.getSize())
                .wearType(
                        variant.getWearType() != null
                                ? variant.getWearType().name()
                                : null
                )

                .sellPrice(variant.getSellPrice())
                .originalPrice(variant.getOriginalPrice())
                .discountPercentage(variant.getDiscountPercentage())

                .image(image)
                .inStock(variant.getStock() > 0)

                /* ---------- UI ---------- */
                .availableSizes(availableSizes)

                /* ---------- META ---------- */
                .rating(product.getRating())
                .createdAt(wishlist.getCreatedAt())

                .build();
    }
}
