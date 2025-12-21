package com.diaperbazaar.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String slug;

    private BrandDTO brand;
    private CategoryDTO category;

    // Default variant = what frontend shows first
    private ProductVariantDTO defaultVariant;

    // For size/variant selector
    private List<ProductVariantDTO> variants;
}
