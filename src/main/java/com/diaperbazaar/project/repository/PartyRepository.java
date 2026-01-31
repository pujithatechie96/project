package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    
    List<Party> findByIsActiveTrueOrderByNameAsc();
    
    List<Party> findByPartyTypeAndIsActiveTrueOrderByNameAsc(Party.PartyType partyType);
    
    List<Party> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    @Query("SELECT p FROM Party p WHERE p.isActive = true AND p.currentBalance > 0 ORDER BY p.currentBalance DESC")
    List<Party> findPartiesWithOutstandingBalance();

}
