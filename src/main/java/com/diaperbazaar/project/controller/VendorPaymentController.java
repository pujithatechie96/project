//package com.diaperbazaar.project.controller;
//
//import com.diaperbazaar.project.dto.VendorPaymentDTO;
//import com.diaperbazaar.project.entity.VendorPayment;
//import com.diaperbazaar.project.service.VendorPaymentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/vendor-payments")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class VendorPaymentController {
//
//    private final VendorPaymentService vendorPaymentService;
//
//    @GetMapping
//    public ResponseEntity<List<VendorPayment>> getPaymentsByVendor(@RequestParam Long vendorId) {
//        return ResponseEntity.ok(vendorPaymentService.findByVendorId(vendorId));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<VendorPayment> getPaymentById(@PathVariable Long id) {
//        return vendorPaymentService.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<VendorPayment> createPayment(@RequestBody VendorPaymentDTO dto) {
//        return ResponseEntity.ok(vendorPaymentService.create(dto));
//    }
//
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<VendorPayment> updateStatus(
//            @PathVariable Long id,
//            @RequestBody Map<String, String> body) {
//        String status = body.get("status");
//        return ResponseEntity.ok(vendorPaymentService.updateStatus(id, status));
//    }
//}
