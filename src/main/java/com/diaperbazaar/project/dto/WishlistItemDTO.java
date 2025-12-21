package com.diaperbazaar.project.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WishlistItemDTO {

    private Long id;

    /* ---------- PRODUCT ---------- */
    private Long productId;
    private String productName;
    private String productSlug;
    private String category;

    /* ---------- VARIANT (SELECTED) ---------- */
    private Long variantId;
    private String variantTitle;
    private String size;
    private String wearType;

    private Double sellPrice;
    private Double originalPrice;
    private Double discountPercentage;

    private String image;
    private boolean inStock;

    /* ---------- UI HELPERS ---------- */
    private List<String> availableSizes;

    /* ---------- META ---------- */
    private Double rating;
    private LocalDateTime createdAt;
}
