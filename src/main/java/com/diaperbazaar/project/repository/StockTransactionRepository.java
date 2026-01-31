package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.StockTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    
    Page<StockTransaction> findAllByOrderByTransactionDateDesc(Pageable pageable);
    
    Page<StockTransaction> findByProductIdOrderByTransactionDateDesc(Long productId, Pageable pageable);
    
    List<StockTransaction> findByPartyIdOrderByTransactionDateDesc(Long partyId);
    
    StockTransaction findTopByProductIdOrderByIdDesc(Long productId);
    
    StockTransaction findTopByProductIdAndVariantIdOrderByIdDesc(Long productId, Long variantId);
    
    @Query("SELECT st FROM StockTransaction st WHERE st.transactionDate BETWEEN :startDate AND :endDate ORDER BY st.transactionDate DESC")
    List<StockTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT st.productId, st.productName, " +
           "SUM(CASE WHEN st.transactionType IN ('PURCHASE', 'STOCK_IN', 'OPENING') THEN st.quantity ELSE 0 END) as totalIn, " +
           "SUM(CASE WHEN st.transactionType IN ('SALE', 'STOCK_OUT', 'DAMAGE') THEN st.quantity ELSE 0 END) as totalOut " +
           "FROM StockTransaction st GROUP BY st.productId, st.productName")
    List<Object[]> getStockSummary();

    @Query("SELECT st.partyId FROM StockTransaction st WHERE st.productId = :productId AND st.variantId = :variantId AND st.transactionType = 'PURCHASE' AND st.partyId IS NOT NULL ORDER BY st.id DESC LIMIT 1")
    Long findLastPurchasePartyId(@Param("productId") Long productId, @Param("variantId") Long variantId);

    @Query("SELECT st.partyId FROM StockTransaction st WHERE st.productId = :productId AND st.variantId = :variantId AND st.transactionType = 'PURCHASE' AND st.partyId IS NOT NULL ORDER BY st.id DESC")
    List<Long> findLastPurchasePartyIds(@Param("productId") Long productId, @Param("variantId") Long variantId);
}
