package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.PartyTransaction;
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
public interface PartyTransactionRepository extends JpaRepository<PartyTransaction, Long> {
    
    List<PartyTransaction> findByPartyIdOrderByTransactionDateDesc(Long partyId);
    
    Page<PartyTransaction> findByPartyIdOrderByTransactionDateDesc(Long partyId, Pageable pageable);
    
    PartyTransaction findTopByPartyIdOrderByIdDesc(Long partyId);
    
    @Query("SELECT pt FROM PartyTransaction pt WHERE pt.partyId = :partyId AND pt.transactionDate BETWEEN :startDate AND :endDate ORDER BY pt.transactionDate DESC")
    List<PartyTransaction> findByPartyIdAndDateRange(
        @Param("partyId") Long partyId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(pt.amount) FROM PartyTransaction pt WHERE pt.partyId = :partyId AND pt.transactionType = 'PURCHASE'")
    BigDecimal sumPurchasesByParty(@Param("partyId") Long partyId);
    
    @Query("SELECT SUM(pt.amount) FROM PartyTransaction pt WHERE pt.partyId = :partyId AND pt.transactionType = 'PAYMENT_OUT'")
    BigDecimal sumPaymentsByParty(@Param("partyId") Long partyId);
}
