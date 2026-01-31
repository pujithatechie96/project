package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.PurchaseOrderDTO;
import com.diaperbazaar.project.dto.ReceiveItemsDTO;
import com.diaperbazaar.project.entity.PurchaseOrder;
import com.diaperbazaar.project.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PurchaseOrderController {
    
    private final PurchaseOrderService purchaseOrderService;
    
    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(purchaseOrderService.findAll(vendorId, status, startDate, endDate));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        return purchaseOrderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(
            @PathVariable Long id, 
            @RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.update(id, dto));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(purchaseOrderService.updateStatus(id, status));
    }
    
    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrder> receiveItems(
            @PathVariable Long id, 
            @RequestBody ReceiveItemsDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.receiveItems(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ResponseEntity.ok().build();
    }
}
