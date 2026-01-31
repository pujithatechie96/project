package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.PurchaseEntryDTO;
import com.diaperbazaar.project.dto.SaleEntryDTO;
import com.diaperbazaar.project.dto.StockAdjustmentDTO;
import com.diaperbazaar.project.entity.StockTransaction;
import com.diaperbazaar.project.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Stock Controller - Vyapar Style
 * REST API for stock/inventory management
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockService stockService;

    /**
     * GET /api/stock/transactions
     * Get all stock transactions (paginated)
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<StockTransaction>> getAllTransactions(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (productId != null) {
            return ResponseEntity.ok(stockService.getTransactionsByProduct(productId, pageable));
        }
        return ResponseEntity.ok(stockService.getAllTransactions(pageable));
    }

    /**
     * GET /api/stock/summary
     * Get stock summary for all products
     */
    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getStockSummary() {
        return ResponseEntity.ok(stockService.getStockSummary());
    }

    /**
     * GET /api/stock/current/{productId}
     * Get current stock for a product/variant
     */
    @GetMapping("/current/{productId}")
    public ResponseEntity<Integer> getCurrentStock(
            @PathVariable Long productId,
            @RequestParam(required = false) Long variantId) {
        return ResponseEntity.ok(stockService.getCurrentStock(productId, variantId));
    }

    /**
     * POST /api/stock/purchase
     * Add purchase entry (stock in from party/vendor)
     */
    @PostMapping("/purchase")
    public ResponseEntity<StockTransaction> addPurchase(@RequestBody PurchaseEntryDTO purchaseDTO) {
        return ResponseEntity.ok(stockService.addPurchase(purchaseDTO,false));
    }

    /**
     * POST /api/stock/sale
     * Add sale entry (stock out to customer)
     */
    @PostMapping("/sale")
    public ResponseEntity<StockTransaction> addSale(@RequestBody SaleEntryDTO saleDTO) {
        return ResponseEntity.ok(stockService.addSale(
                saleDTO.getProductId(),
                saleDTO.getVariantId(),
                saleDTO.getPartyId(),
                saleDTO.getQuantity(),
                saleDTO.getUnitPrice(),
                saleDTO.getNotes()
        ));
    }

    /**
     * POST /api/stock/adjustment
     * Add stock adjustment (in/out/damage/opening)
     */
    @PostMapping("/adjustment")
    public ResponseEntity<StockTransaction> addAdjustment(@RequestBody StockAdjustmentDTO adjustmentDTO) {
        return ResponseEntity.ok(stockService.addStockAdjustment(adjustmentDTO));
    }
}
