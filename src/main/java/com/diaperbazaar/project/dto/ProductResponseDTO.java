package com.diaperbazaar.project.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String slug;

    private Double rating;
    private Integer reviewCount;

    @Column(unique = true)
    private String sku;

    private String productType;

    private BrandDTO brand;
    private CategoryDTO category;

    // Default variant = what frontend shows first
    private ProductVariantDTO defaultVariant;

    // For size/variant selector
    private List<ProductVariantDTO> variants;
}
