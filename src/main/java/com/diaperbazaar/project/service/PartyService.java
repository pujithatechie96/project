package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.PartyPaymentDTO;
import com.diaperbazaar.project.entity.Party;
import com.diaperbazaar.project.entity.PartyTransaction;
import com.diaperbazaar.project.repository.PartyRepository;
import com.diaperbazaar.project.repository.PartyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Party Service - Vyapar Style
 * Handles party (vendor/customer) operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {
    
    private final PartyRepository partyRepository;
    private final PartyTransactionRepository partyTransactionRepository;
    
    /**
     * Get all parties
     */
    public List<Party> getAllParties() {
        return partyRepository.findByIsActiveTrueOrderByNameAsc();
    }
    
    /**
     * Get parties by type
     */
    public List<Party> getPartiesByType(Party.PartyType type) {
        return partyRepository.findByPartyTypeAndIsActiveTrueOrderByNameAsc(type);
    }
    
    /**
     * Get party by ID
     */
    public Party getPartyById(Long id) {
        return partyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Party not found"));
    }
    
    /**
     * Create new party
     */
    public Party createParty(Party party) {
        party.setCurrentBalance(party.getCurrentBalance());
        party.setIsActive(true);
        return partyRepository.save(party);
    }
    
    /**
     * Update party
     */
    public Party updateParty(Long id, Party partyDetails) {
        Party party = getPartyById(id);
        party.setName(partyDetails.getName());
        party.setPhone(partyDetails.getPhone());
        party.setEmail(partyDetails.getEmail());
        party.setAddress(partyDetails.getAddress());
        party.setPartyType(partyDetails.getPartyType());
        party.setGstNumber(partyDetails.getGstNumber());
        return partyRepository.save(party);
    }
    
    /**
     * Delete (soft delete) party
     */
    public void deleteParty(Long id) {
        Party party = getPartyById(id);
        party.setIsActive(false);
        partyRepository.save(party);
    }
    
    /**
     * Get party ledger (all transactions)
     */
    public List<PartyTransaction> getPartyLedger(Long partyId) {
        return partyTransactionRepository.findByPartyIdOrderByTransactionDateDesc(partyId);
    }
    
    /**
     * Get party summary
     */
    public Map<String, Object> getPartySummary(Long partyId) {
        Party party = getPartyById(partyId);
        
        BigDecimal totalPurchases = partyTransactionRepository.sumPurchasesByParty(partyId);
        BigDecimal totalPayments = partyTransactionRepository.sumPaymentsByParty(partyId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("party", party);
        summary.put("totalPurchases", totalPurchases != null ? totalPurchases : BigDecimal.ZERO);
        summary.put("totalPayments", totalPayments != null ? totalPayments : BigDecimal.ZERO);
        summary.put("currentBalance", party.getCurrentBalance());
        
        return summary;
    }
    
    /**
     * Record payment to party
     */
    public PartyTransaction recordPayment(PartyPaymentDTO dto) {
        Party party = getPartyById(dto.getPartyId());
        
        // Calculate new balance (payment reduces what we owe)
        BigDecimal newBalance = party.getCurrentBalance().subtract(dto.getAmount());
        
        // Create party transaction
        PartyTransaction txn = new PartyTransaction();
        txn.setTransactionDate(LocalDateTime.now());
        txn.setPartyId(dto.getPartyId());
        txn.setTransactionType(PartyTransaction.TransactionType.PAYMENT_OUT);
        txn.setAmount(dto.getAmount());
        txn.setPaymentMode(PartyTransaction.PaymentMode.valueOf(dto.getPaymentMode()));
        txn.setPaymentReference(dto.getPaymentReference());
        txn.setBalanceAfter(newBalance);
        txn.setDescription("Payment: " + dto.getPaymentMode());
        txn.setNotes(dto.getNotes());
        
        txn = partyTransactionRepository.save(txn);
        
        // Update party balance
        party.setCurrentBalance(newBalance);
        partyRepository.save(party);
        
        return txn;
    }
    
    /**
     * Get parties with outstanding balance (we owe them)
     */
    public List<Party> getPartiesWithOutstandingBalance() {
        return partyRepository.findPartiesWithOutstandingBalance();
    }
    
    /**
     * Search parties by name
     */
    public List<Party> searchParties(String keyword) {
        return partyRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
    }
}
