package com.diaperbazaar.project.service;


import com.diaperbazaar.project.dto.PurchaseEntryDTO;
import com.diaperbazaar.project.dto.StockAdjustmentDTO;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stock Service - Vyapar Style
 * Handles all stock operations: purchases, sales, adjustments
 * Integrates with existing Product and ProductVariant entities
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockService {
    
    private final StockTransactionRepository stockTransactionRepository;
    private final PartyRepository partyRepository;
    private final PartyTransactionRepository partyTransactionRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PartyService partyService;
    
    /**
     * Get all stock transactions (paginated)
     */
    public Page<StockTransaction> getAllTransactions(Pageable pageable) {
        return stockTransactionRepository.findAllByOrderByTransactionDateDesc(pageable);
    }
    
    /**
     * Get transactions by product
     */
    public Page<StockTransaction> getTransactionsByProduct(Long productId, Pageable pageable) {
        return stockTransactionRepository.findByProductIdOrderByTransactionDateDesc(productId, pageable);
    }
    
    /**
     * Get stock summary for all products
     */
    public List<Map<String, Object>> getStockSummary() {
        List<Object[]> results = stockTransactionRepository.getStockSummary();
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", row[0]);
            item.put("productName", row[1]);
            item.put("totalIn", row[2] != null ? row[2] : 0);
            item.put("totalOut", row[3] != null ? row[3] : 0);
            
            Long totalIn = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long totalOut = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            item.put("currentStock", totalIn - totalOut);
            
            summary.add(item);
        }
        
        return summary;
    }
    
    /**
     * Add Purchase Entry - Stock IN from Party/Vendor
     * Creates: Stock Transaction, Party Transaction, Updates balances
     */
    public StockTransaction addPurchase(PurchaseEntryDTO dto, boolean isFirsttransaction) {
        // Get current stock
        int currentStock = getCurrentStock(dto.getProductId(), dto.getVariantId());
        int newStock = currentStock + dto.getQuantity();
        if(isFirsttransaction){
            newStock = dto.getQuantity();
        }

        // Get party
        Party party = partyRepository.findById(dto.getPartyId())
            .orElseThrow(() -> new RuntimeException("Party not found: " + dto.getPartyId()));
        
        // Get product name
        String productName = getProductName(dto.getProductId());
        
        BigDecimal totalAmount = dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        BigDecimal newPartyBalance = party.getCurrentBalance().add(totalAmount);
        
        // Create stock transaction
        StockTransaction stockTxn = new StockTransaction();
        stockTxn.setTransactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now());
        stockTxn.setTransactionType(StockTransaction.TransactionType.PURCHASE);
        stockTxn.setProductId(dto.getProductId());
        stockTxn.setVariantId(dto.getVariantId());
        stockTxn.setProductName(productName);
        stockTxn.setPartyId(dto.getPartyId());
        stockTxn.setPartyName(party.getName());
        stockTxn.setQuantity(dto.getQuantity());
        stockTxn.setUnitPrice(dto.getUnitPrice());
        stockTxn.setTotalAmount(totalAmount);
        stockTxn.setBalanceAfter(newStock);
        stockTxn.setNotes(dto.getNotes());
        
        stockTxn = stockTransactionRepository.save(stockTxn);
        
        // Create party transaction (ledger entry)
        PartyTransaction partyTxn = new PartyTransaction();
        partyTxn.setTransactionDate(stockTxn.getTransactionDate());
        partyTxn.setPartyId(dto.getPartyId());
        partyTxn.setTransactionType(PartyTransaction.TransactionType.PURCHASE);
        partyTxn.setAmount(totalAmount);
        partyTxn.setBalanceAfter(newPartyBalance);
        partyTxn.setStockTransactionId(stockTxn.getId());
        partyTxn.setDescription("Purchase: " + productName + " x " + dto.getQuantity());
        partyTxn.setNotes(dto.getNotes());
        
        partyTransactionRepository.save(partyTxn);
        
        // Update party balance
        party.setCurrentBalance(newPartyBalance);
        partyRepository.save(party);
        
        // Update ProductVariant stock
        if(!isFirsttransaction)
            updateProductVariantStock(dto.getVariantId(), newStock);
        
        return stockTxn;
    }
    
    /**
     * Add Sale Entry - Stock OUT to Customer
     */
    public StockTransaction addSale(Long productId, Long variantId, Long partyId,
                                     int quantity, BigDecimal unitPrice, String notes) {
        // Get current stock
        int currentStock = getCurrentStock(productId, variantId);
        
        // Check if sufficient stock
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + currentStock + ", Requested: " + quantity);
        }
        
        int newStock = currentStock - quantity;
        
        // Get product name
        String productName = getProductName(productId);
        
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        // Create stock transaction
        StockTransaction stockTxn = new StockTransaction();
        stockTxn.setTransactionDate(LocalDateTime.now());
        stockTxn.setTransactionType(StockTransaction.TransactionType.SALE);
        stockTxn.setProductId(productId);
        stockTxn.setVariantId(variantId);
        stockTxn.setProductName(productName);
        stockTxn.setQuantity(quantity);
        stockTxn.setUnitPrice(unitPrice);
        stockTxn.setTotalAmount(totalAmount);
        stockTxn.setBalanceAfter(newStock);
        stockTxn.setNotes(notes);
        
        // If party exists (customer ledger)
        if (partyId != null) {
            Party party = partyRepository.findById(partyId).orElse(null);
            if (party != null) {
                stockTxn.setPartyId(partyId);
                stockTxn.setPartyName(party.getName());
                
                // Sale increases what customer owes (debit)
                BigDecimal newPartyBalance = party.getCurrentBalance().add(totalAmount);
                
                // Create party transaction
                PartyTransaction partyTxn = new PartyTransaction();
                partyTxn.setTransactionDate(stockTxn.getTransactionDate());
                partyTxn.setPartyId(partyId);
                partyTxn.setTransactionType(PartyTransaction.TransactionType.SALE);
                partyTxn.setAmount(totalAmount);
                partyTxn.setBalanceAfter(newPartyBalance);
                partyTxn.setDescription("Sale: " + productName + " x " + quantity);
                partyTxn.setNotes(notes);
                
                stockTxn = stockTransactionRepository.save(stockTxn);
                partyTxn.setStockTransactionId(stockTxn.getId());
                partyTransactionRepository.save(partyTxn);
                
                // Update party balance
                party.setCurrentBalance(newPartyBalance);
                partyRepository.save(party);
            }
        } else {
            stockTxn = stockTransactionRepository.save(stockTxn);
        }
        
        // Update ProductVariant stock
        updateProductVariantStock(variantId, newStock);
        
        return stockTxn;
    }
    
    /**
     * Add Stock Adjustment (In/Out/Damage/Opening)
     */
    public StockTransaction addStockAdjustment(StockAdjustmentDTO dto) {
        int currentStock = getCurrentStock(dto.getProductId(), dto.getVariantId());
        int newStock;
        
        StockTransaction.TransactionType type = StockTransaction.TransactionType.valueOf(dto.getAdjustmentType().toUpperCase());
        
        // Calculate new stock based on type
        switch (type) {
            case STOCK_IN:
                PurchaseEntryDTO dto1 = new PurchaseEntryDTO();
                dto1.setPartyId(dto.getPartyId());
                dto1.setProductId(dto.getProductId());
                dto1.setVariantId(dto.getVariantId());
                dto1.setQuantity(dto.getQuantity());
                dto1.setUnitPrice(dto.getUnitPrice());
                dto1.setTransactionDate(dto.getTransactionDate());
                dto1.setNotes(dto.getNotes());
                return addPurchase(dto1,false);
            case OPENING:
                newStock = (type == StockTransaction.TransactionType.OPENING) ? dto.getQuantity() : currentStock + dto.getQuantity();
                break;
            case STOCK_OUT:
            case DAMAGE:
                newStock = currentStock - dto.getQuantity();
                if (newStock < 0) {
                    throw new RuntimeException("Cannot reduce stock below zero. Current: " + currentStock);
                }
                break;
            default:
                throw new RuntimeException("Invalid adjustment type: " + dto.getAdjustmentType());
        }
        
        // Get product name
        String productName = getProductName(dto.getProductId());
        
        // Create stock transaction
        StockTransaction stockTxn = new StockTransaction();
        stockTxn.setTransactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now());
        stockTxn.setTransactionType(type);
        stockTxn.setProductId(dto.getProductId());
        stockTxn.setVariantId(dto.getVariantId());
        stockTxn.setProductName(productName);
        stockTxn.setQuantity(dto.getQuantity());
        stockTxn.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO);
        stockTxn.setTotalAmount(dto.getUnitPrice() != null ? 
            dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())) : BigDecimal.ZERO);
        stockTxn.setBalanceAfter(newStock);
        stockTxn.setNotes(dto.getNotes());
        stockTxn.setPartyId(dto.getPartyId());
        stockTxn.setPartyName(dto.getPartyName());
        
        stockTxn = stockTransactionRepository.save(stockTxn);
        
        // Update ProductVariant stock
        updateProductVariantStock(dto.getVariantId(), newStock);
        
        return stockTxn;
    }
    
    /**
     * Get current stock for a product/variant from last transaction
     */
    public int getCurrentStock(Long productId, Long variantId) {
        if (variantId != null) {
            return productVariantRepository.findById(variantId)
                    .map(ProductVariant::getStock)
                    .orElse(0);
        }
        // Fallback: sum stock of all variants for the product
        return productRepository.findById(productId)
                .map(product -> product.getVariants().stream()
                        .mapToInt(ProductVariant::getStock)
                        .sum())
                .orElse(0);
    }


    /**
     * Get product name from Product entity
     */
    private String getProductName(Long productId) {
        return productRepository.findById(productId)
            .map(Product::getName)
            .orElse("Unknown Product #" + productId);
    }
    
    /**
     * Update ProductVariant stock field
     */
    private void updateProductVariantStock(Long variantId, int newStock) {
        if (variantId != null) {
            productVariantRepository.findById(variantId).ifPresent(variant -> {
                variant.setStock(newStock);
                // Update inStock flag based on stock level
                // variant.setInStock(newStock > 0);
                productVariantRepository.save(variant);
            });
        }
    }
}
