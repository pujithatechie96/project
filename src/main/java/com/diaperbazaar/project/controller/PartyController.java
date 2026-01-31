package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.PartyPaymentDTO;
import com.diaperbazaar.project.entity.Party;
import com.diaperbazaar.project.entity.PartyTransaction;
import com.diaperbazaar.project.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Party Controller - Vyapar Style
 * REST API for party (vendor/customer) management
 */
@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartyController {
    
    private final PartyService partyService;
    
    /**
     * GET /api/parties
     * Get all parties (optionally filter by type)
     */
    @GetMapping
    public ResponseEntity<List<Party>> getAllParties(
            @RequestParam(required = false) String type) {
        
        if (type != null && !type.isEmpty()) {
            return ResponseEntity.ok(partyService.getPartiesByType(Party.PartyType.valueOf(type.toUpperCase())));
        }
        return ResponseEntity.ok(partyService.getAllParties());
    }
    
    /**
     * GET /api/parties/{id}
     * Get party by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Party> getPartyById(@PathVariable Long id) {
        return ResponseEntity.ok(partyService.getPartyById(id));
    }
    
    /**
     * POST /api/parties
     * Create new party
     */
    @PostMapping
    public ResponseEntity<Party> createParty(@RequestBody Party party) {
        return ResponseEntity.ok(partyService.createParty(party));
    }
    
    /**
     * PUT /api/parties/{id}
     * Update party
     */
    @PutMapping("/{id}")
    public ResponseEntity<Party> updateParty(@PathVariable Long id, @RequestBody Party party) {
        return ResponseEntity.ok(partyService.updateParty(id, party));
    }
    
    /**
     * DELETE /api/parties/{id}
     * Delete (soft delete) party
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParty(@PathVariable Long id) {
        partyService.deleteParty(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/parties/{id}/ledger
     * Get party ledger (all transactions)
     */
    @GetMapping("/{id}/ledger")
    public ResponseEntity<List<PartyTransaction>> getPartyLedger(@PathVariable Long id) {
        return ResponseEntity.ok(partyService.getPartyLedger(id));
    }
    
    /**
     * GET /api/parties/{id}/summary
     * Get party summary
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getPartySummary(@PathVariable Long id) {
        return ResponseEntity.ok(partyService.getPartySummary(id));
    }
    
    /**
     * POST /api/parties/{id}/payment
     * Record payment to party
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<PartyTransaction> recordPayment(
            @PathVariable Long id,
            @RequestBody PartyPaymentDTO paymentDTO) {
        paymentDTO.setPartyId(id);
        return ResponseEntity.ok(partyService.recordPayment(paymentDTO));
    }
    
    /**
     * GET /api/parties/outstanding
     * Get parties with outstanding balance
     */
    @GetMapping("/outstanding")
    public ResponseEntity<List<Party>> getPartiesWithOutstandingBalance() {
        return ResponseEntity.ok(partyService.getPartiesWithOutstandingBalance());
    }
    
    /**
     * GET /api/parties/search?keyword=xxx
     * Search parties
     */
    @GetMapping("/search")
    public ResponseEntity<List<Party>> searchParties(@RequestParam String keyword) {
        return ResponseEntity.ok(partyService.searchParties(keyword));
    }
}
