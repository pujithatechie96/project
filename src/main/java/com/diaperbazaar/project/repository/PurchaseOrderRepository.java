package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();
    
    List<PurchaseOrder> findByVendorId(Long vendorId);
    
    List<PurchaseOrder> findByStatus(PurchaseOrder.PurchaseOrderStatus status);
    
    List<PurchaseOrder> findByVendorIdAndStatus(Long vendorId, PurchaseOrder.PurchaseOrderStatus status);
}
