package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.InventoryAdjustmentDTO;
import com.diaperbazaar.project.entity.InventoryLedger;
import com.diaperbazaar.project.service.InventoryLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-ledger")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryLedgerController {
    
    private final InventoryLedgerService inventoryLedgerService;
    
    @GetMapping
    public ResponseEntity<Page<InventoryLedger>> getLedgerEntries(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        return ResponseEntity.ok(inventoryLedgerService.findAll(
                productId, variantId, vendorId, transactionType,
                startDate, endDate, PageRequest.of(page, size)));
    }
    
    @GetMapping("/summary")
    public ResponseEntity<?> getInventorySummary(@RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(inventoryLedgerService.getSummary(productId));
    }
    
    @PostMapping("/adjustment")
    public ResponseEntity<InventoryLedger> createAdjustment(@RequestBody InventoryAdjustmentDTO dto) {
        return ResponseEntity.ok(inventoryLedgerService.createAdjustment(dto));
    }
}
