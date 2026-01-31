package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.OrderItem;
import com.diaperbazaar.project.entity.Product;
import com.diaperbazaar.project.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.hpsf.Variant;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private ProductVariant variant;   // ✅ NEW
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private String size;
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private Long appliedOfferId;
    private String appliedOfferName;
    private Integer billableQty;
    private Integer deliveredQty;


    // ✅ ENTITY → DTO MAPPER
    public static OrderItemDTO fromEntity(OrderItem item) {

        OrderItemDTO dto = new OrderItemDTO();

        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setVariant(item.getVariant());
        dto.setProductName(item.getProductName());
        dto.setProductImage(item.getProductImage());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSize(item.getSize());
        dto.setGstPercentage(item.getGstPercentage());
        dto.setGstAmount(item.getGstAmount());
        dto.setTotalAmount(item.getTotalAmount());
        dto.setSubTotal(item.getSubTotal());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setAppliedOfferId(item.getAppliedOfferId());
        dto.setAppliedOfferName(item.getAppliedOfferName());
        dto.setBillableQty(item.getBillableQty());
        dto.setDeliveredQty(item.getDeliveredQty());

        return dto;
    }
}
