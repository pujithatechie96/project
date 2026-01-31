package com.diaperbazaar.project.service;

import com.diaperbazaar.project.entity.PurchaseOrder;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorLedgerService {
    
    private final VendorLedgerRepository vendorLedgerRepository;
    private final VendorRepository vendorRepository;
    
    public Page<VendorLedger> findAll(Long vendorId, String transactionType,
                                       String startDate, String endDate, Pageable pageable) {
        if (vendorId != null) {
            return vendorLedgerRepository.findByVendorIdOrderByTransactionDateDesc(vendorId, pageable);
        }
        return vendorLedgerRepository.findAllByOrderByTransactionDateDesc(pageable);
    }
    
    public Map<String, Object> getSummary(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("vendorId", vendorId);
        summary.put("vendorName", vendor.getName());
        summary.put("currentBalance", vendor.getCurrentBalance());
        
        BigDecimal totalDebit = vendorLedgerRepository.sumDebitByVendorId(vendorId);
        BigDecimal totalCredit = vendorLedgerRepository.sumCreditByVendorId(vendorId);
        
        summary.put("totalDebit", totalDebit != null ? totalDebit : BigDecimal.ZERO);
        summary.put("totalCredit", totalCredit != null ? totalCredit : BigDecimal.ZERO);
        
        return summary;
    }
    
    public void createPurchaseEntry(PurchaseOrder po) {
        Vendor vendor = po.getVendor();
        BigDecimal currentBalance = vendor.getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(po.getTotalAmount());
        
        VendorLedger entry = new VendorLedger();
        entry.setVendor(vendor);
        entry.setTransactionDate(LocalDateTime.now());
        entry.setTransactionType(VendorLedger.VendorTransactionType.PURCHASE);
        entry.setReferenceType(VendorLedger.VendorReferenceType.PURCHASE_ORDER);
        entry.setReferenceId(po.getId());
        entry.setReferenceNumber(po.getPoNumber());
        entry.setDescription("Purchase Order: " + po.getPoNumber());
        entry.setCreditAmount(po.getTotalAmount());
        entry.setBalance(newBalance);
        
        vendorLedgerRepository.save(entry);
        
        // Update vendor balance
        vendor.setCurrentBalance(newBalance);
        vendorRepository.save(vendor);
    }
    
//    public void createPaymentEntry(VendorPayment payment) {
//        Vendor vendor = payment.getVendor();
//        BigDecimal currentBalance = vendor.getCurrentBalance();
//        BigDecimal newBalance = currentBalance.subtract(payment.getAmount());
//
//        VendorLedger entry = new VendorLedger();
//        entry.setVendor(vendor);
//        entry.setTransactionDate(LocalDateTime.now());
//        entry.setTransactionType(VendorLedger.VendorTransactionType.PAYMENT);
//        entry.setReferenceType(VendorLedger.VendorReferenceType.PAYMENT);
//        entry.setReferenceId(payment.getId());
//        entry.setReferenceNumber(payment.getPaymentNumber());
//        entry.setDescription("Payment: " + payment.getPaymentNumber());
//        entry.setDebitAmount(payment.getAmount());
//        entry.setBalance(newBalance);
//        entry.setPaymentMode(VendorLedger.PaymentMode.valueOf(payment.getPaymentMode().name()));
//        entry.setPaymentReference(payment.getPaymentReference());
//
//        vendorLedgerRepository.save(entry);
//
//        // Update vendor balance
//        vendor.setCurrentBalance(newBalance);
//        vendorRepository.save(vendor);
//    }
}
