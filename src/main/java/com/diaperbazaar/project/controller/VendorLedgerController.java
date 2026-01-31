package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.entity.VendorLedger;
import com.diaperbazaar.project.service.VendorLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor-ledger")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VendorLedgerController {
    
    private final VendorLedgerService vendorLedgerService;
    
    @GetMapping
    public ResponseEntity<Page<VendorLedger>> getLedgerEntries(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        return ResponseEntity.ok(vendorLedgerService.findAll(
                vendorId, transactionType, startDate, endDate,
                PageRequest.of(page, size)));
    }
    
    @GetMapping("/summary")
    public ResponseEntity<?> getVendorLedgerSummary(@RequestParam Long vendorId) {
        return ResponseEntity.ok(vendorLedgerService.getSummary(vendorId));
    }
}
