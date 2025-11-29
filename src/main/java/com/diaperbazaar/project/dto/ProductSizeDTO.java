package com.diaperbazaar.project.dto;
import lombok.Data;


public class ProductSizeDTO {
    private Long id;
    private String size;
    private Integer stock;
    private Double price;
    private String sku;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
}