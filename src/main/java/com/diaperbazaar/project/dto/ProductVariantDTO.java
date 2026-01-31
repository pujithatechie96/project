package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.ProductVariant;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductVariantDTO {

    private Long id;
    private String title;
    private String size;
    private Integer packCount;

    private List<String> images;
    private String image;
    private String description;
    private List<String> features;

    private Double originalPrice;
    private Double sellPrice;
    private Double offlineSellPrice;
    private Double buyPrice;
    private Double discountPercentage;
    private BigDecimal gstPercentage;

    private Integer stock;
    private ProductVariant.WearType wearType;
    private Boolean isDefault;
    private Boolean inStock;
    private String visibility;
    private String sku;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getPackCount() {
        return packCount;
    }

    public void setPackCount(Integer packCount) {
        this.packCount = packCount;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Double getOriginalPrice() {
        return originalPrice;
    }


}
