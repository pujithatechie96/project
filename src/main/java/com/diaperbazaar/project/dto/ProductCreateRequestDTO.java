package com.diaperbazaar.project.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductCreateRequestDTO {

    private String name;
    private Long id;
    private String slug;
    private String productType;

    private Long brandId;
    private Long categoryId;
    private Double rating;
    private Integer reviewCount;

    private List<ProductVariantDTO> variants;

    private Long partyId;
    private Boolean createPurchaseRecord;
}
