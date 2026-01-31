package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.entity.Vendor;
import com.diaperbazaar.project.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VendorController {
    
    private final VendorService vendorService;
    
    @GetMapping
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable Long id) {
        return vendorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Vendor>> searchVendors(@RequestParam String keyword) {
        return ResponseEntity.ok(vendorService.search(keyword));
    }
    
    @PostMapping
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        return ResponseEntity.ok(vendorService.save(vendor));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor vendor) {
        return vendorService.findById(id)
                .map(existing -> {
                    vendor.setId(id);
                    return ResponseEntity.ok(vendorService.save(vendor));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        if (vendorService.findById(id).isPresent()) {
            vendorService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{id}/ledger")
    public ResponseEntity<?> getVendorLedger(
            @PathVariable Long id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(vendorService.getVendorLedger(id, startDate, endDate));
    }
    
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getVendorSummary(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getVendorSummary(id));
    }
}
