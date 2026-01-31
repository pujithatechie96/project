//package com.diaperbazaar.project.service;
//
//import com.diaperbazaar.project.dto.VendorPaymentDTO;
//import com.diaperbazaar.project.entity.*;
//import com.diaperbazaar.project.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class VendorPaymentService {
//
//    private final VendorPaymentRepository vendorPaymentRepository;
//    private final VendorRepository vendorRepository;
//    private final PurchaseOrderRepository purchaseOrderRepository;
//    private final VendorLedgerService vendorLedgerService;
//
//    public List<VendorPayment> findByVendorId(Long vendorId) {
//        return vendorPaymentRepository.findByVendorIdOrderByPaymentDateDesc(vendorId);
//    }
//
//    public Optional<VendorPayment> findById(Long id) {
//        return vendorPaymentRepository.findById(id);
//    }
//
//    public VendorPayment create(VendorPaymentDTO dto) {
//        Vendor vendor = vendorRepository.findById(dto.getVendorId())
//                .orElseThrow(() -> new RuntimeException("Vendor not found"));
//
//        VendorPayment payment = new VendorPayment();
//        payment.setPaymentNumber(generatePaymentNumber());
//        payment.setVendor(vendor);
//        payment.setPaymentDate(dto.getPaymentDate());
//        payment.setAmount(dto.getAmount());
//        payment.setPaymentMode(VendorPayment.PaymentMode.valueOf(dto.getPaymentMode()));
//        payment.setPaymentReference(dto.getPaymentReference());
//        payment.setBankName(dto.getBankName());
//        payment.setChequeNumber(dto.getChequeNumber());
//        payment.setChequeDate(dto.getChequeDate());
//        payment.setNotes(dto.getNotes());
//        payment.setStatus(VendorPayment.PaymentStatus.COMPLETED);
//
//        VendorPayment saved = vendorPaymentRepository.save(payment);
//
//        // Create vendor ledger entry
//        vendorLedgerService.createPaymentEntry(saved);
//
//        // Allocate to purchase orders if provided
//        if (dto.getAllocations() != null) {
//            for (VendorPaymentDTO.AllocationDTO allocation : dto.getAllocations()) {
//                PurchaseOrder po = purchaseOrderRepository.findById(allocation.getPurchaseOrderId())
//                        .orElseThrow(() -> new RuntimeException("Purchase order not found"));
//
//                po.setAmountPaid(po.getAmountPaid().add(allocation.getAllocatedAmount()));
//
//                // Update payment status
//                if (po.getAmountPaid().compareTo(po.getTotalAmount()) >= 0) {
//                    po.setPaymentStatus(PurchaseOrder.PaymentStatus.PAID);
//                } else if (po.getAmountPaid().compareTo(java.math.BigDecimal.ZERO) > 0) {
//                    po.setPaymentStatus(PurchaseOrder.PaymentStatus.PARTIALLY_PAID);
//                }
//
//                purchaseOrderRepository.save(po);
//            }
//        }
//
//        return saved;
//    }
//
//    public VendorPayment updateStatus(Long id, String status) {
//        VendorPayment payment = vendorPaymentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        payment.setStatus(VendorPayment.PaymentStatus.valueOf(status));
//        return vendorPaymentRepository.save(payment);
//    }
//
//    private String generatePaymentNumber() {
//        Long count = vendorPaymentRepository.count() + 1;
//        return String.format("PAY-%06d", count);
//    }
//}
