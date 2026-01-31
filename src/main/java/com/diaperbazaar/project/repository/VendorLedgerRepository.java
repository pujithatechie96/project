package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.VendorLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {
    
    Page<VendorLedger> findAllByOrderByTransactionDateDesc(Pageable pageable);
    
    Page<VendorLedger> findByVendorIdOrderByTransactionDateDesc(Long vendorId, Pageable pageable);
    
    List<VendorLedger> findByVendorIdOrderByTransactionDateDesc(Long vendorId);
    
    List<VendorLedger> findByVendorIdAndTransactionDateBetween(
            Long vendorId, LocalDateTime start, LocalDateTime end);
    
    List<VendorLedger> findByVendorIdAndTransactionDateAfter(Long vendorId, LocalDateTime start);
    
    @Query("SELECT SUM(v.debitAmount) FROM VendorLedger v WHERE v.vendor.id = :vendorId")
    BigDecimal sumDebitByVendorId(@Param("vendorId") Long vendorId);
    
    @Query("SELECT SUM(v.creditAmount) FROM VendorLedger v WHERE v.vendor.id = :vendorId")
    BigDecimal sumCreditByVendorId(@Param("vendorId") Long vendorId);
}
