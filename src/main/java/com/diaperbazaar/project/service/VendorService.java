package com.diaperbazaar.project.service;

import com.diaperbazaar.project.entity.Vendor;
import com.diaperbazaar.project.entity.VendorLedger;
import com.diaperbazaar.project.repository.VendorRepository;
import com.diaperbazaar.project.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorService {
    
    private final VendorRepository vendorRepository;
    private final VendorLedgerRepository vendorLedgerRepository;
    
    public List<Vendor> findAll() {
        return vendorRepository.findAllByOrderByNameAsc();
    }
    
    public Optional<Vendor> findById(Long id) {
        return vendorRepository.findById(id);
    }
    
    public List<Vendor> search(String keyword) {
        return vendorRepository.searchByKeyword(keyword);
    }
    
    public Vendor save(Vendor vendor) {
        boolean isNew = vendor.getId() == null;
        Vendor saved = vendorRepository.save(vendor);
        
        // Create opening balance ledger entry for new vendors
        if (isNew && vendor.getOpeningBalance() != null && 
            vendor.getOpeningBalance().compareTo(BigDecimal.ZERO) != 0) {
            createOpeningBalanceEntry(saved);
        }
        
        return saved;
    }
    
    public void deleteById(Long id) {
        vendorRepository.deleteById(id);
    }
    
    public List<VendorLedger> getVendorLedger(Long vendorId, String startDate, String endDate) {
        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(23, 59, 59) : null;
        
        if (start != null && end != null) {
            return vendorLedgerRepository.findByVendorIdAndTransactionDateBetween(vendorId, start, end);
        } else if (start != null) {
            return vendorLedgerRepository.findByVendorIdAndTransactionDateAfter(vendorId, start);
        } else {
            return vendorLedgerRepository.findByVendorIdOrderByTransactionDateDesc(vendorId);
        }
    }
    
    public Map<String, Object> getVendorSummary(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("vendor", vendor);
        summary.put("currentBalance", vendor.getCurrentBalance());
        summary.put("creditLimit", vendor.getCreditLimit());
        summary.put("availableCredit", vendor.getCreditLimit().subtract(vendor.getCurrentBalance()));
        
        // Add more summary data as needed
        return summary;
    }
    
    private void createOpeningBalanceEntry(Vendor vendor) {
        VendorLedger entry = new VendorLedger();
        entry.setVendor(vendor);
        entry.setTransactionDate(LocalDateTime.now());
        entry.setTransactionType(VendorLedger.VendorTransactionType.OPENING_BALANCE);
        entry.setReferenceType(VendorLedger.VendorReferenceType.MANUAL);
        entry.setDescription("Opening Balance");
        entry.setCreditAmount(vendor.getOpeningBalance());
        entry.setBalance(vendor.getOpeningBalance());
        vendorLedgerRepository.save(entry);
        
        vendor.setCurrentBalance(vendor.getOpeningBalance());
        vendorRepository.save(vendor);
    }
}
