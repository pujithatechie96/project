package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.InventoryAdjustmentDTO;
import com.diaperbazaar.project.entity.InventoryLedger;
import com.diaperbazaar.project.entity.PurchaseOrder;
import com.diaperbazaar.project.entity.PurchaseOrderItem;
import com.diaperbazaar.project.repository.InventoryLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryLedgerService {
    
    private final InventoryLedgerRepository inventoryLedgerRepository;
    
    public Page<InventoryLedger> findAll(Long productId, Long variantId, Long vendorId,
                                          String transactionType, String startDate, 
                                          String endDate, Pageable pageable) {
        // Implement filtering logic
        if (productId != null) {
            return inventoryLedgerRepository.findByProductIdOrderByTransactionDateDesc(productId, pageable);
        }
        return inventoryLedgerRepository.findAllByOrderByTransactionDateDesc(pageable);
    }
    
    public Map<String, Object> getSummary(Long productId) {
        Map<String, Object> summary = new HashMap<>();
        
        if (productId != null) {
            Integer totalIn = inventoryLedgerRepository.sumQuantityInByProductId(productId);
            Integer totalOut = inventoryLedgerRepository.sumQuantityOutByProductId(productId);
            summary.put("productId", productId);
            summary.put("totalIn", totalIn != null ? totalIn : 0);
            summary.put("totalOut", totalOut != null ? totalOut : 0);
            summary.put("balance", (totalIn != null ? totalIn : 0) - (totalOut != null ? totalOut : 0));
        }
        
        return summary;
    }
    
    public InventoryLedger createAdjustment(InventoryAdjustmentDTO dto) {
        // Get current balance
        Integer currentBalance = getCurrentBalance(dto.getProductId(), dto.getVariantId());
        
        InventoryLedger entry = new InventoryLedger();
        entry.setTransactionDate(LocalDateTime.now());
        entry.setTransactionType(InventoryLedger.TransactionType.valueOf(dto.getTransactionType()));
        entry.setReferenceType(InventoryLedger.ReferenceType.ADJUSTMENT);
        entry.setProductId(dto.getProductId());
        entry.setVariantId(dto.getVariantId());
        entry.setNotes(dto.getNotes());
        
        if (dto.getTransactionType().equals("ADJUSTMENT_IN")) {
            entry.setQuantityIn(dto.getQuantity());
            entry.setBalanceQuantity(currentBalance + dto.getQuantity());
        } else {
            entry.setQuantityOut(dto.getQuantity());
            entry.setBalanceQuantity(currentBalance - dto.getQuantity());
        }
        
        entry.setUnitCost(dto.getUnitCost());
        entry.setTotalValue(dto.getUnitCost() != null ? 
                dto.getUnitCost().multiply(BigDecimal.valueOf(dto.getQuantity())) : null);
        
        return inventoryLedgerRepository.save(entry);
    }
    
    public void createPurchaseEntry(PurchaseOrder po, PurchaseOrderItem item, int quantityReceived) {
        Integer currentBalance = getCurrentBalance(item.getProductId(), item.getVariantId());
        
        InventoryLedger entry = new InventoryLedger();
        entry.setTransactionDate(LocalDateTime.now());
        entry.setTransactionType(InventoryLedger.TransactionType.PURCHASE);
        entry.setReferenceType(InventoryLedger.ReferenceType.PURCHASE_ORDER);
        entry.setReferenceId(po.getId());
        entry.setReferenceNumber(po.getPoNumber());
        entry.setProductId(item.getProductId());
        entry.setVariantId(item.getVariantId());
        entry.setVendorId(po.getVendor().getId());
        entry.setQuantityIn(quantityReceived);
        entry.setBalanceQuantity(currentBalance + quantityReceived);
        entry.setUnitCost(item.getUnitPrice());
        entry.setTotalValue(item.getUnitPrice().multiply(BigDecimal.valueOf(quantityReceived)));
        entry.setNotes("Received from PO: " + po.getPoNumber());
        
        inventoryLedgerRepository.save(entry);
        
        // Update product variant stock (you'll need to implement this)
        // productVariantRepository.updateStock(item.getVariantId(), quantityReceived);
    }
    
    private Integer getCurrentBalance(Long productId, Long variantId) {
        InventoryLedger lastEntry = inventoryLedgerRepository
                .findTopByProductIdAndVariantIdOrderByIdDesc(productId, variantId);
        return lastEntry != null ? lastEntry.getBalanceQuantity() : 0;
    }
}
