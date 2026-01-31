package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.PurchaseOrderDTO;
import com.diaperbazaar.project.dto.ReceiveItemsDTO;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final VendorRepository vendorRepository;
    private final InventoryLedgerService inventoryLedgerService;
    private final VendorLedgerService vendorLedgerService;
    
    public List<PurchaseOrder> findAll(Long vendorId, String status, String startDate, String endDate) {
        // Implement filtering logic based on parameters
        if (vendorId != null) {
            return purchaseOrderRepository.findByVendorId(vendorId);
        }
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Optional<PurchaseOrder> findById(Long id) {
        return purchaseOrderRepository.findById(id);
    }
    
    public PurchaseOrder create(PurchaseOrderDTO dto) {
        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber());
        po.setVendor(vendor);
        po.setOrderDate(LocalDate.parse(dto.getOrderDate()));
        if (dto.getExpectedDeliveryDate() != null) {
            po.setExpectedDeliveryDate(LocalDate.parse(dto.getExpectedDeliveryDate()));
        }
        po.setNotes(dto.getNotes());
        po.setStatus(PurchaseOrder.PurchaseOrderStatus.DRAFT);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (PurchaseOrderDTO.ItemDTO itemDto : dto.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProductId(itemDto.getProductId());
            item.setVariantId(itemDto.getVariantId());
            item.setQuantityOrdered(itemDto.getQuantityOrdered());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setTaxPercentage(itemDto.getTaxPercentage() != null ? itemDto.getTaxPercentage() : BigDecimal.ZERO);
            item.setDiscountPercentage(itemDto.getDiscountPercentage() != null ? itemDto.getDiscountPercentage() : BigDecimal.ZERO);
            item.calculateTotals();
            
            subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered())));
            totalTax = totalTax.add(item.getTaxAmount());
            totalDiscount = totalDiscount.add(item.getDiscountAmount());
            
            po.addItem(item);
        }
        
        po.setSubtotal(subtotal);
        po.setTaxAmount(totalTax);
        po.setDiscountAmount(totalDiscount);
        po.setTotalAmount(subtotal.subtract(totalDiscount).add(totalTax));
        
        return purchaseOrderRepository.save(po);
    }
    
    public PurchaseOrder update(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        
        if (po.getStatus() != PurchaseOrder.PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Can only edit draft orders");
        }
        
        // Update logic similar to create
        return purchaseOrderRepository.save(po);
    }
    
    public PurchaseOrder updateStatus(Long id, String status) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        
        PurchaseOrder.PurchaseOrderStatus newStatus = PurchaseOrder.PurchaseOrderStatus.valueOf(status);
        po.setStatus(newStatus);
        
        // If approved, create vendor ledger entry
        if (newStatus == PurchaseOrder.PurchaseOrderStatus.APPROVED) {
            vendorLedgerService.createPurchaseEntry(po);
        }
        
        return purchaseOrderRepository.save(po);
    }
    
    public PurchaseOrder receiveItems(Long id, ReceiveItemsDTO dto) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        
        boolean allReceived = true;
        
        for (ReceiveItemsDTO.ItemReceive itemReceive : dto.getItems()) {
            PurchaseOrderItem item = purchaseOrderItemRepository.findById(itemReceive.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            
            item.setQuantityReceived(item.getQuantityReceived() + itemReceive.getQuantityReceived());
            purchaseOrderItemRepository.save(item);
            
            // Create inventory ledger entry
            inventoryLedgerService.createPurchaseEntry(po, item, itemReceive.getQuantityReceived());
            
            if (item.getQuantityReceived() < item.getQuantityOrdered()) {
                allReceived = false;
            }
        }
        
        // Update PO status
        if (allReceived) {
            po.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED);
            po.setActualDeliveryDate(LocalDate.now());
        } else {
            po.setStatus(PurchaseOrder.PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
        
        return purchaseOrderRepository.save(po);
    }
    
    public void delete(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        
        if (po.getStatus() != PurchaseOrder.PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Can only delete draft orders");
        }
        
        purchaseOrderRepository.delete(po);
    }
    
    private String generatePoNumber() {
        Long count = purchaseOrderRepository.count() + 1;
        return String.format("PO-%06d", count);
    }
}
