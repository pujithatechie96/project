package com.diaperbazaar.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockVariantDTO {
    private String categoryName;
    private String categorySlug;
    private String variantTitle;
    private Long variantId;
    private Integer stock;
}
