package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
}
