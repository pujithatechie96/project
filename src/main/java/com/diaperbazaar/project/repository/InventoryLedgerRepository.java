package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.InventoryLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, Long> {
    
    Page<InventoryLedger> findAllByOrderByTransactionDateDesc(Pageable pageable);
    
    Page<InventoryLedger> findByProductIdOrderByTransactionDateDesc(Long productId, Pageable pageable);
    
    InventoryLedger findTopByProductIdAndVariantIdOrderByIdDesc(Long productId, Long variantId);
    
    InventoryLedger findTopByProductIdOrderByIdDesc(Long productId);
    
    @Query("SELECT SUM(i.quantityIn) FROM InventoryLedger i WHERE i.productId = :productId")
    Integer sumQuantityInByProductId(@Param("productId") Long productId);
    
    @Query("SELECT SUM(i.quantityOut) FROM InventoryLedger i WHERE i.productId = :productId")
    Integer sumQuantityOutByProductId(@Param("productId") Long productId);
}
